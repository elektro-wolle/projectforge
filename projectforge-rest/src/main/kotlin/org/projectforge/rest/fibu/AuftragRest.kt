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

package org.projectforge.rest.fibu

import org.projectforge.business.fibu.AuftragDO
import org.projectforge.business.fibu.AuftragDao
import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDORest
import org.projectforge.ui.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${Rest.URL}/order")
class AuftragRest() : AbstractDORest<AuftragDO, AuftragDao, BaseSearchFilter>(AuftragDao::class.java, BaseSearchFilter::class.java, "fibu.auftrag.title") {

    /**
     * LAYOUT List page
     */
    override fun createListLayout(): UILayout {
        val layout = super.createListLayout()
                .add(UITable.UIResultSetTable()
                        .add(lc, "nummer", "kunde", "projekt", "titel", "positionen", "personDays", "referenz", "assignedPersons", "erfassungsDatum", "entscheidungsDatum", "nettoSumme", "beauftragtNettoSumme", "fakturiertSum", "zuFakturierenSum", "periodOfPerformanceBegin", "periodOfPerformanceEnd", "probabilityOfOccurrence"))
        layout.getTableColumnById("kunde").formatter = Formatter.CUSTOMER
        layout.getTableColumnById("projekt").formatter = Formatter.PROJECT
        layout.getTableColumnById("positionen").formatter = Formatter.AUFTRAG_POSITION
        layout.getTableColumnById("erfassungsDatum").formatter = Formatter.DATE
        layout.getTableColumnById("entscheidungsDatum").formatter = Formatter.DATE
        layout.getTableColumnById("periodOfPerformanceBegin").formatter = Formatter.DATE
        layout.getTableColumnById("periodOfPerformanceEnd").formatter = Formatter.DATE
        return LayoutUtils.processListPage(layout)
    }

    /**
     * LAYOUT Edit page
     */
    override fun createEditLayout(dataObject: AuftragDO): UILayout {
        val layout = super.createEditLayout(dataObject)
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "nummer", "nettoSumme")))
                .add(UIRow()
                        .add(UICol()
                                .add(lc, "title")))
        return LayoutUtils.processEditPage(layout, dataObject)
    }
}