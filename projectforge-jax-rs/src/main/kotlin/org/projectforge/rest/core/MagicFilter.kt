package org.projectforge.rest.core

import org.projectforge.framework.persistence.api.BaseSearchFilter

class MagicFilter<F : BaseSearchFilter>(
        /**
         * Optional searchfilter of ProjectForge's entities, such as [org.projectforge.business.address.AddressFilter],
         * [org.projectforge.business.timesheet.TimesheetFilter] etc.
         */
        val searchFilter: F? = null,
        /**
         * Optional entries for searching (keywords, field search, range search etc.)
         */
        val entries: MutableList<MagicFilterEntry>? = null
) {
    /**
     * Creates the search filter for the data-base query.
     * @param filterClass Needed for creating a new filter instance if not yet given.
     *
     * Please note: Range search and search for values must be implemented for every specific filter.
     */
    fun prepareQueryFilter(filterClass: Class<F>): F {
        val filter = searchFilter ?: filterClass.newInstance()
        if (entries.isNullOrEmpty()) {
            return filter // Nothing to configure.
        }
        val searchStrings = mutableListOf<String>()
        entries.forEach { entry ->
            when (entry.type()) {
                MagicFilterEntry.Type.STRING_SEARCH -> {
                    searchStrings.add("${entry.getSearchStringStrategy()}")
                }
                MagicFilterEntry.Type.FIELD_STRING_SEARCH -> {
                    searchStrings.add("${entry.field}:${entry.getSearchStringStrategy()}")
                }
            }
        }
        return filter
    }
}