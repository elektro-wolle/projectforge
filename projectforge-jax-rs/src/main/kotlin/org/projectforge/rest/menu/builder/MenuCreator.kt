package org.projectforge.rest.menu.builder

import org.projectforge.business.configuration.ConfigurationService
import org.projectforge.business.fibu.*
import org.projectforge.business.fibu.datev.DatevImportDao.hasRight
import org.projectforge.business.fibu.kost.Kost2Dao
import org.projectforge.business.humanresources.HRPlanningDao
import org.projectforge.business.user.ProjectForgeGroup
import org.projectforge.business.user.UserRightId
import org.projectforge.business.user.UserRightValue
import org.projectforge.framework.access.AccessChecker
import org.projectforge.framework.configuration.Configuration
import org.projectforge.framework.persistence.api.UserRightService.*
import org.projectforge.rest.menu.MenuItem
import org.projectforge.sms.SmsSenderConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MenuCreator() {
    internal class MenuHolder() {
        internal val menuItems: MutableList<MenuItemDef> = mutableListOf()
        fun add(menuItem: MenuItemDef): MenuItemDef {
            menuItems.add(menuItem)
            return menuItem
        }
    }

    private val menu = MenuHolder()

    private var initialized = false

    @Autowired
    private lateinit var accessChecker: AccessChecker

    @Autowired
    private lateinit var smsSenderConfig: SmsSenderConfig

    @Autowired
    private lateinit var configurationService: ConfigurationService

    @Synchronized
    private fun initialize() {
        if (initialized) return
        val commonMenu = menu.add(MenuItemDef(MenuItemDefId.COMMON))
                .add(MenuItemDef(MenuItemDefId.CALENDAR, "wa/teamCalendar"))
                .add(MenuItemDef(MenuItemDefId.TEAMCALENDAR, "wa/wicket/bookmarkable/org.projectforge.web.teamcal.admin.TeamCalListPage"))
                .add(MenuItemDef(MenuItemDefId.VACATION, "wa/wicket/bookmarkable/org.projectforge.web.vacation.VacationListPage")) // MenuNewCounterVacation()
                .add(MenuItemDef(MenuItemDefId.BOOK_LIST, "books"))
                .add(MenuItemDef(MenuItemDefId.ADDRESSBOOK_LIST, "wa/wicket/bookmarkable/org.projectforge.web.address.AddressbookListPage"))
                .add(MenuItemDef(MenuItemDefId.ADDRESS_LIST, "addresses"))
        if (configurationService.telephoneSystemUrl.isNotEmpty())
            commonMenu.add(MenuItemDef(MenuItemDefId.PHONE_CALL, "wa/phoneCall"))
        if (smsSenderConfig.isSmsConfigured())
            commonMenu.add(MenuItemDef(MenuItemDefId.SEND_SMS, "wa/sendSms"))
        if (Configuration.getInstance().isMebConfigured())
            commonMenu.add(MenuItemDef(MenuItemDefId.MEB, "wa/mebList")) // MenuNewCounterMeb
        commonMenu.add(MenuItemDef(MenuItemDefId.SEARCH, "wa/search"))

        var pmMenu = menu.add(MenuItemDef(MenuItemDefId.PROJECT_MANAGEMENT))
                .add(MenuItemDef(MenuItemDefId.TASK_TREE, "wa/taskTree"))
                .add(MenuItemDef(MenuItemDefId.TIMESHEET_LIST, "wa/timesheetList"))
                .add(MenuItemDef(MenuItemDefId.MONTHLY_EMPLOYEE_REPORT, "wa/monthlyEmployeeReport"))
                .add(MenuItemDef(MenuItemDefId.PERSONAL_STATISTICS, "wa/personalStatistics"))
                .add(MenuItemDef(MenuItemDefId.HR_VIEW, "wa/hrList",
                        checkAccess = { hasRight(HRPlanningDao.USER_RIGHT_ID, *READONLY_READWRITE) }))
                // HRPlanningDao.USER_RIGHT_ID, *READONLY_READWRITE
                .add(MenuItemDef(MenuItemDefId.HR_PLANNING_LIST, "wa/hrPlanningList"))
                .add(MenuItemDef(MenuItemDefId.GANTT, "wa/ganttList"))
                // MenuNewCounterOrder, tooltip = "menu.fibu.orderbook.htmlSuffixTooltip"
                .add(MenuItemDef(MenuItemDefId.ORDER_LIST, "wa/orderBookList",
                        checkAccess = {
                            hasRight(AuftragDao.USER_RIGHT_ID, *READONLY_PARTLYREADWRITE_READWRITE) &&
                                    !isInGroup(*FIBU_ORGA_GROUPS) // Orderbook is shown under menu FiBu for FiBu users
                        }))

        var hrMenu = menu.add(MenuItemDef(MenuItemDefId.HR,
                checkAccess = { isInGroup(ProjectForgeGroup.HR_GROUP) }))
                .add(MenuItemDef(MenuItemDefId.EMPLOYEE_LIST, "wa/employeeList",
                        checkAccess = {
                            hasRight(EmployeeDao.USER_RIGHT_ID, *READONLY_READWRITE)
                        }))
                .add(MenuItemDef(MenuItemDefId.EMPLOYEE_SALARY_LIST, "wa/employeeSalaryList",
                        checkAccess = {
                            hasRight(EmployeeSalaryDao.USER_RIGHT_ID, *READONLY_READWRITE)
                        }))

        var fibuMenu = menu.add(MenuItemDef(MenuItemDefId.FIBU,
                checkAccess = { isInGroup(*FIBU_ORGA_GROUPS) }))
                .add(MenuItemDef(MenuItemDefId.OUTGOING_INVOICE_LIST, "wa/outgoingInvoiceList",
                        checkAccess = {
                            hasRight(RechnungDao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                    isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                        }))
                .add(MenuItemDef(MenuItemDefId.INCOMING_INVOICE_LIST, "wa/incomingInvoiceList",
                        checkAccess = {
                            hasRight(EingangsrechnungDao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                    isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                        }))
        if (Configuration.getInstance().isCostConfigured()) {
            fibuMenu.add(MenuItemDef(MenuItemDefId.CUSTOMER_LIST, "wa/customerList",
                    checkAccess = { isInGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP) }))
                    .add(MenuItemDef(MenuItemDefId.PROJECT_LIST, "wa/projectList",
                            checkAccess = {
                                hasRight(ProjektDao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                        isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                            }))
        }
        // MenuNewCounterOrder, tooltip = "menu.fibu.orderbook.htmlSuffixTooltip"
        fibuMenu.add(MenuItemDef(MenuItemDefId.ORDER_LIST, "wa/orderBookList",
                checkAccess = { isInGroup(*FIBU_ORGA_GROUPS) }))

        if (Configuration.getInstance().isCostConfigured()) {
            menu.add(MenuItemDef(MenuItemDefId.COST,
                    checkAccess = {
                        isInGroup(*FIBU_ORGA_HR_GROUPS, ProjectForgeGroup.CONTROLLING_GROUP)
                    }))
                    .add(MenuItemDef(MenuItemDefId.ACCOUNT_LIST, "wa/accountList",
                            checkAccess = {
                                hasRight(KontoDao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                        isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                            }))
                    .add(MenuItemDef(MenuItemDefId.COST1_LIST, "wa/cost1List",
                            checkAccess = {
                                hasRight(Kost2Dao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                        isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                            }))
                    .add(MenuItemDef(MenuItemDefId.COST2_LIST, "wa/cost2List",
                            checkAccess = {
                                hasRight(Kost2Dao.USER_RIGHT_ID, *READONLY_READWRITE) ||
                                        isInGroup(ProjectForgeGroup.CONTROLLING_GROUP)
                            }))
        }
        menu.add(MenuItemDef(MenuItemDefId.REPORTING,
                checkAccess = {
                    isInGroup(*FIBU_ORGA_HR_GROUPS)
                }))
        menu.add(MenuItemDef(MenuItemDefId.ORGA,
                checkAccess = {
                    isInGroup(*FIBU_ORGA_HR_GROUPS)
                })) // UserRightService.FIBU_ORGA_HR_GROUPS
        menu.add(MenuItemDef(MenuItemDefId.ADMINISTRATION)) // .setVisibleForRestrictedUsers(true)
        menu.add(MenuItemDef(MenuItemDefId.MISC))


/*

        // REPORTING
        reg.register(reporting, MenuItemDefId.SCRIPT_LIST, 10, ScriptListPage::class.java, FINANCE_GROUP, CONTROLLING_GROUP)
        reg.register(reporting, MenuItemDefId.SCRIPTING, 20, ScriptingPage::class.java, FINANCE_GROUP, CONTROLLING_GROUP)
        reg.register(reporting, MenuItemDefId.REPORT_OBJECTIVES, 30, ReportObjectivesPage::class.java, FINANCE_GROUP,
                CONTROLLING_GROUP)
        run {
            // Only visible if cost is configured and DATEV-Import right is given:
            reg.register(reporting, MenuItemDefId.ACCOUNTING_RECORD_LIST, 40, AccountingRecordListPage::class.java, DatevImportDao.USER_RIGHT_ID,
                    UserRightValue.TRUE)
            reg.register(reporting, MenuItemDefId.DATEV_IMPORT, 50, DatevImportPage::class.java, DatevImportDao.USER_RIGHT_ID,
                    UserRightValue.TRUE)
        }

        // ORGA
        reg.register(orga, MenuItemDefId.OUTBOX_LIST, 10, PostausgangListPage::class.java, PostausgangDao.USER_RIGHT_ID,
                *READONLY_READWRITE)
        reg.register(orga, MenuItemDefId.INBOX_LIST, 20, PosteingangListPage::class.java, PosteingangDao.USER_RIGHT_ID,
                *READONLY_READWRITE)
        reg.register(orga, MenuItemDefId.CONTRACTS, 30, ContractListPage::class.java, ContractDao.USER_RIGHT_ID,
                *READONLY_READWRITE)
        reg.register(orga, MenuItemDefId.VISITORBOOK, 30, VisitorbookListPage::class.java, VisitorbookDao.USER_RIGHT_ID,
                *READONLY_READWRITE)

        // ADMINISTRATION
        reg.register(admin, MenuItemDefId.MY_ACCOUNT, 10, MyAccountEditPage::class.java)
        reg.register(
                object : MenuItemDef(admin, MenuItemDefId.VACATION_VIEW.getId(), 11, MenuItemDefId.VACATION_VIEW.getI18nKey(),
                        VacationViewPage::class.java) {
                    protected override fun isVisible(context: MenuBuilderContext): Boolean {
                        return vacationService.couldUserUseVacationService(ThreadLocalUserContext.getUser(), false)
                    }
                }
        )
        reg.register(admin, MenuItemDefId.MY_PREFERENCES, 20, UserPrefListPage::class.java)
        reg.register(
                object : MenuItemDef(admin, MenuItemDefId.CHANGE_PASSWORD.getId(), 30, MenuItemDefId.CHANGE_PASSWORD.getI18nKey(),
                        ChangePasswordPage::class.java) {
                    /**
                     * @see org.projectforge.web.MenuItemDef.isVisible
                     */
                    protected override fun isVisible(context: MenuBuilderContext): Boolean {
                        // The visibility of this menu entry is evaluated by the login handler implementation.
                        val user = context.getLoggedInUser()
                        return Login.getInstance().isPasswordChangeSupported(user)
                    }
                })

        reg.register(
                object : MenuItemDef(admin, MenuItemDefId.CHANGE_WLAN_PASSWORD.getId(), 32, MenuItemDefId.CHANGE_WLAN_PASSWORD.getI18nKey(),
                        ChangeWlanPasswordPage::class.java) {
                    protected override fun isVisible(context: MenuBuilderContext): Boolean {
                        // The visibility of this menu entry is evaluated by the login handler implementation.
                        val user = context.getLoggedInUser()
                        return Login.getInstance().isWlanPasswordChangeSupported(user)
                    }
                })

        reg.register(object : MenuItemDef(admin, MenuItemDefId.TENANT_LIST.getId(), 35, MenuItemDefId.TENANT_LIST.getI18nKey(),
                TenantListPage::class.java) {
            /**
             * @see org.projectforge.web.MenuItemDef.isVisible
             */
            protected override fun isVisible(context: MenuBuilderContext): Boolean {
                val user = context.getLoggedInUser()
                return TenantChecker.isSuperAdmin<ExtendedBaseDO<Int>>(user)
            }
        })
        // reg.register(admin, MenuItemDefId.TENANT_LIST, 35, TenantListPage.class, TenantDao.USER_RIGHT_ID, READONLY_READWRITE);
        reg.register(admin, MenuItemDefId.USER_LIST, 40, UserListPage::class.java)
        reg.register(admin, MenuItemDefId.GROUP_LIST, 50, GroupListPage::class.java) // Visible for all.
        reg.register(admin, MenuItemDefId.ACCESS_LIST, 60, AccessListPage::class.java) // Visible for all.
        reg.register(admin, MenuItemDefId.SYSTEM, 70, AdminPage::class.java, ADMIN_GROUP)
        // Only available in development mode or if SQL console is configured in SecurityConfig.
        reg.register(admin, MenuItemDefId.SQL_CONSOLE, 71, SqlConsolePage::class.java, ADMIN_GROUP)
        reg.register(admin, MenuItemDefId.GROOVY_CONSOLE, 72, GroovyConsolePage::class.java, ADMIN_GROUP)
        reg.register(admin, MenuItemDefId.LUCENE_CONSOLE, 72, LuceneConsolePage::class.java, ADMIN_GROUP)
        reg.register(admin, MenuItemDefId.SYSTEM_UPDATE, 80, SystemUpdatePage::class.java, ADMIN_GROUP)
        reg.register(admin, MenuItemDefId.SYSTEM_STATISTICS, 90, SystemStatisticsPage::class.java)
        reg.register(admin, MenuItemDefId.CONFIGURATION, 100, ConfigurationListPage::class.java, ADMIN_GROUP)
        reg.register(admin, MenuItemDefId.PLUGIN_ADMIN, 110, PluginListPage::class.java, ADMIN_GROUP)
*/
        initialized = true
    }

    fun build(menuBuilderContext: MenuCreatorContext): List<MenuItem> {
        initialize()
        val root = MenuItem("root")
        menu.menuItems.forEach { menuItemDef ->
            build(root, menuItemDef, menuBuilderContext)
        }
        return root.subMenu!!
    }

    private fun build(parent: MenuItem, menuItemDef: MenuItemDef, menuBuilderContext: MenuCreatorContext) {
        if (menuItemDef.checkAccess?.invoke() == false)
            return
        val menuItem = menuItemDef.createMenu(menuBuilderContext)
        parent.add(menuItem)
        menuItemDef.childs?.forEach { childMenuItemDef ->
            build(menuItem, childMenuItemDef, menuBuilderContext)
        }
    }

    private fun hasRight(rightId: UserRightId, vararg values: UserRightValue): Boolean {
        return accessChecker.hasLoggedInUserRight(rightId, false, *values)
    }

    private fun isInGroup(vararg groups: ProjectForgeGroup): Boolean {
        return accessChecker.isLoggedInUserMemberOfGroup(*groups)
    }
}