/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.rest

import org.projectforge.business.address.AddressbookDO
import org.projectforge.business.address.AddressbookDao
import org.projectforge.business.group.service.GroupService
import org.projectforge.business.user.service.UserService
import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDTORest
import org.projectforge.rest.dto.Addressbook
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/addressBook")
class AddressBookRest : AbstractDTORest<AddressbookDO, Addressbook, AddressbookDao>(
        AddressbookDao::class.java,
        "addressbook.title"
) {
    @Autowired
    private lateinit var groupService: GroupService

    @Autowired
    private lateinit var userService: UserService

    // Needed to use as dto.
    override fun transformFromDB(obj: AddressbookDO, editMode: Boolean): Addressbook {
        val addressbook = Addressbook()
        addressbook.copyFrom(obj)
        // Group names needed by React client (for ReactSelect):
        addressbook.fullAccessGroups?.forEach { it.displayName = groupService.getGroupname(it.id) }
        addressbook.readonlyAccessGroups?.forEach { it.displayName = groupService.getGroupname(it.id) }
        // Usernames needed by React client (for ReactSelect):
        addressbook.fullAccessUsers?.forEach { it.displayName = userService.getUser(it.id)?.displayName ?: "" }
        addressbook.readonlyAccessUsers?.forEach { it.displayName = userService.getUser(it.id)?.displayName ?: "" }
        return addressbook
    }

    // Needed to use as dto.
    override fun transformForDB(dto: Addressbook): AddressbookDO {
        val addressbookDO = AddressbookDO()
        dto.copyTo(addressbookDO)
        return addressbookDO
    }

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.UIResultSetTable()
                        .add(lc, "title", "description", "owner", "accessright", "last_update"))
        layout.getTableColumnById("owner").formatter = Formatter.USER
        layout.getTableColumnById("last_update").formatter = Formatter.TIMESTAMP_MINUTES
        return LayoutUtils.processListPage(layout, this)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dto: Addressbook, userAccess: UILayout.UserAccess): UILayout {
        val layout = super.createEditLayout(dto, userAccess)
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "title")
                                .add(lc, "description"))
                        .add(UICol()
                                .add(lc, "owner")))
                .add(UIRow()
                        .add(UICol()
                                .add(UISelect<Int>("fullAccessUsers", lc,
                                        multi = true,
                                        label = "addressbook.fullAccess",
                                        additionalLabel = "access.users",
                                        autoCompletion = AutoCompletion<Int>(url = "user/${AutoCompletion.AUTOCOMPLETE_OBJECT}?search=",
                                                type = AutoCompletion.Type.USER.name),
                                        labelProperty = "displayName",
                                        valueProperty = "id",
                                        tooltip = "addressbook.fullAccess.tooltip"))
                                .add(UISelect<Int>("readonlyAccessUsers", lc,
                                        multi = true,
                                        label = "addressbook.readonlyAccess",
                                        additionalLabel = "access.users",
                                        autoCompletion = AutoCompletion<Int>(url = "user/${AutoCompletion.AUTOCOMPLETE_OBJECT}?search=",
                                                type = AutoCompletion.Type.USER.name),
                                        labelProperty = "displayName",
                                        valueProperty = "id",
                                        tooltip = "addressbook.readonlyAccess.tooltip")))
                        .add(UICol()
                                .add(UISelect<Int>("fullAccessGroups", lc,
                                        multi = true,
                                        label = "addressbook.fullAccess",
                                        additionalLabel = "access.groups",
                                        autoCompletion = AutoCompletion<Int>(url = "group/${AutoCompletion.AUTOCOMPLETE_OBJECT}?search=",
                                                type = AutoCompletion.Type.GROUP.name),
                                        labelProperty = "displayName",
                                        valueProperty = "id",
                                        tooltip = "addressbook.fullAccess.tooltip"))
                                .add(UISelect<Int>("readonlyAccessGroups", lc,
                                        multi = true,
                                        label = "addressbook.readonlyAccess",
                                        additionalLabel = "access.groups",
                                        autoCompletion = AutoCompletion<Int>(url = "group/${AutoCompletion.AUTOCOMPLETE_OBJECT}?search=",
                                                type = AutoCompletion.Type.GROUP.name),
                                        labelProperty = "displayName",
                                        valueProperty = "id",
                                        tooltip = "addressbook.readonlyAccess.tooltip"))))
        return LayoutUtils.processEditPage(layout, dto, this)
    }

    override val autoCompleteSearchFields = arrayOf("title", "description")

    override fun queryAutocompleteObjects(filter: BaseSearchFilter): MutableList<AddressbookDO> {
        val list = super.queryAutocompleteObjects(filter)
        return list
    }
}
