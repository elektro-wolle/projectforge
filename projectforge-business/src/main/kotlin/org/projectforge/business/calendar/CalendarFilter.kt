/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.business.calendar

import com.thoughtworks.xstream.annotations.XStreamAsAttribute
import org.projectforge.business.teamcal.filter.TemplateEntry
import org.projectforge.favorites.AbstractFavorite

/**
 * Persist the settings of one named filter entry. The user may configure a list of filters and my switch the active
 * calendar filter.
 *
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * @author K. Reinhard (k.reinhard@micromata.de)
 */
class CalendarFilter(name: String = "",
                     id: Int = 0,
                     /**
                      * New items created in the calendar will be assumed as entries of this calendar. If null, then the creation
                      * page for new time sheets is instantiated.
                      */
                     @XStreamAsAttribute
                     var defaultCalendarId: Int? = null,

                     @XStreamAsAttribute
                     var showBirthdays: Boolean? = null,

                     @XStreamAsAttribute
                     var showStatistics: Boolean? = null,

                     /**
                      * Display the time sheets of the user with this id. If null, no time sheets are displayed.
                      */
                     @XStreamAsAttribute
                     var timesheetUserId: Int? = null,

                     /**
                      * If true, own time sheets are displayed. It depends on the user rights if [showTimesheets] or [timesheetUserId] is used.
                      */
                     @XStreamAsAttribute
                     var showTimesheets: Boolean? = null,

                     @XStreamAsAttribute
                     var showBreaks: Boolean? = true,

                     @XStreamAsAttribute
                     var showPlanning: Boolean? = null)
    : AbstractFavorite(name, id) {

    var calendarIds = mutableSetOf<Int>()
        set(value) {
            field = value
            ensureSets()
        }

    var invisibleCalendars = mutableSetOf<Int>()

    fun addCalendarId(calendarId: Int) {
        ensureSets()
        calendarIds.add(calendarId)
        invisibleCalendars.remove(calendarId) // New added calendars should be visible.
    }

    fun removeCalendarId(calendarId: Int) {
        ensureSets()
        calendarIds.remove(calendarId)
        invisibleCalendars.remove(calendarId)
    }

    fun setVisibility(calendarId: Int, visible: Boolean) {
        ensureSets()
        if (visible) {
            invisibleCalendars.remove(calendarId)
        } else {
            invisibleCalendars.add(calendarId)
        }
    }

    fun isVisible(calendarId: Int): Boolean {
        ensureSets()
        return calendarIds.contains(calendarId) && !invisibleCalendars.contains(calendarId)
    }

    /**
     * Sets may be null directly after deserialization. This method also tidies up the list of invisible calendars by
     * removing invisible calendars not contained in the main calendar set.
     */
    @Suppress("SENSELESS_COMPARISON")
    fun ensureSets() {
        if (calendarIds == null) calendarIds = mutableSetOf() // Might be null after deserialization.
        if (invisibleCalendars == null) invisibleCalendars = mutableSetOf() // Might be null after deserialization.
        else
            invisibleCalendars.removeIf { !calendarIds.contains(it) } // Tidy up: remove invisible ids if not in main list.
        val nullValue: Int? = null
        calendarIds.remove(nullValue) // Might occur after deserialization.
        invisibleCalendars.remove(nullValue) // Might occur after deserialization.
    }

    companion object {
        // LEGACY STUFF:

        /**
         * For re-using legacy filters (from ProjetForge version up to 6, Wicket-Calendar).
         */
        internal fun copyFrom(templateEntry: TemplateEntry?): CalendarFilter {
            val displayFilter = CalendarFilter()
            if (templateEntry != null) {
                displayFilter.defaultCalendarId = templateEntry.defaultCalendarId
                displayFilter.name = templateEntry.name
                displayFilter.showBirthdays = templateEntry.isShowBirthdays
                displayFilter.showBreaks = templateEntry.isShowBreaks
                displayFilter.showPlanning = templateEntry.isShowPlanning
                displayFilter.showStatistics = templateEntry.isShowStatistics
                displayFilter.timesheetUserId = templateEntry.timesheetUserId
                displayFilter.showTimesheets = templateEntry.isShowTimesheets
                templateEntry.calendarProperties?.forEach {
                    displayFilter.addCalendarId(it.calId)
                }
            }
            return displayFilter
        }
    }

}
