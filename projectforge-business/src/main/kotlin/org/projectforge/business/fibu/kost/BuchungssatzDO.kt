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

package org.projectforge.business.fibu.kost

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Transient
import javax.persistence.UniqueConstraint

import org.apache.commons.lang3.StringUtils
import org.hibernate.search.annotations.Analyze
import org.hibernate.search.annotations.DateBridge
import org.hibernate.search.annotations.EncodingType
import org.hibernate.search.annotations.Field
import org.hibernate.search.annotations.Index
import org.hibernate.search.annotations.Indexed
import org.hibernate.search.annotations.IndexedEmbedded
import org.hibernate.search.annotations.Resolution
import org.hibernate.search.annotations.Store
import org.projectforge.business.fibu.KontoDO
import org.projectforge.common.StringHelper
import org.projectforge.framework.persistence.entities.DefaultBaseDO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import de.micromata.genome.db.jpa.history.api.WithHistory
import org.projectforge.common.anots.PropertyInfo

/**
 * Repräsentiert einen importierten Datev-Buchungssatz. Die Buchungssätze bilden die Grundlage für
 * betriebwirtschaftliche Auswertungen.
 */
@Entity
@Indexed
@Table(name = "t_fibu_buchungssatz", uniqueConstraints = [UniqueConstraint(columnNames = ["year", "month", "satznr", "tenant_id"])], indexes = [javax.persistence.Index(name = "idx_fk_t_fibu_buchungssatz_gegenkonto_id", columnList = "gegenkonto_id"), javax.persistence.Index(name = "idx_fk_t_fibu_buchungssatz_konto_id", columnList = "konto_id"), javax.persistence.Index(name = "idx_fk_t_fibu_buchungssatz_kost1_id", columnList = "kost1_id"), javax.persistence.Index(name = "idx_fk_t_fibu_buchungssatz_kost2_id", columnList = "kost2_id"), javax.persistence.Index(name = "idx_fk_t_fibu_buchungssatz_tenant_id", columnList = "tenant_id")])
@WithHistory
class BuchungssatzDO : DefaultBaseDO(), Comparable<BuchungssatzDO> {

    private val log = LoggerFactory.getLogger(BuchungssatzDO::class.java)

    /**
     * Jahr zu der die Buchung gehört.
     *
     * @return
     */
    @Field(analyze = Analyze.NO)
    @get:Column(nullable = false)
    var year: Int? = null

    /**
     * Monat zu der die Buchung gehört.
     *
     * @return
     */
    @Field(index = Index.NO, analyze = Analyze.NO)
    @get:Column(nullable = false)
    var month: Int? = null

    @PropertyInfo(i18nKey = "fibu.buchungssatz.satznr")
    @Field(index = Index.NO, analyze = Analyze.NO)
    @get:Column(nullable = false)
    var satznr: Int? = null

    @PropertyInfo(i18nKey = "fibu.common.betrag")
    @get:Column(nullable = false, scale = 2, precision = 18)
    var betrag: BigDecimal? = null
        set(betrag) {
            field = betrag?.setScale(2, RoundingMode.HALF_UP)
        }

    @Field(index = Index.NO, analyze = Analyze.NO)
    @get:Enumerated(EnumType.STRING)
    @get:Column(length = 7, nullable = false)
    var sh: SHType? = null

    @get:Transient
    var isIgnore = false

    @PropertyInfo(i18nKey = "fibu.buchungssatz.konto")
    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "konto_id", nullable = false)
    var konto: KontoDO? = null

    @PropertyInfo(i18nKey = "fibu.buchungssatz.gegenKonto")
    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "gegenkonto_id", nullable = false)
    var gegenKonto: KontoDO? = null

    @Field(index = Index.YES, analyze = Analyze.NO)
    @DateBridge(resolution = Resolution.DAY, encoding = EncodingType.STRING)
    @get:Column(nullable = false)
    var datum: Date? = null

    /** Je nach Buchungssatz: Belegnummer / Referenznummer / Rechnungsnummer.  */
    @PropertyInfo(i18nKey = "fibu.buchungssatz.beleg")
    @Field
    @get:Column(length = 255)
    var beleg: String? = null

    /** Der Buchungstext.  */
    @PropertyInfo(i18nKey = "fibu.buchungssatz.text")
    @Field
    @get:Column(length = 255, name = "buchungstext")
    var text: String? = null

    @Field
    @get:Column(length = 255)
    var menge: String? = null

    @PropertyInfo(i18nKey = "fibu.kost1")
    @IndexedEmbedded(depth = 1)
    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "kost1_id", nullable = false)
    var kost1: Kost1DO? = null

    @PropertyInfo(i18nKey = "fibu.kost2")
    @IndexedEmbedded(depth = 3)
    @get:ManyToOne(fetch = FetchType.EAGER)
    @get:JoinColumn(name = "kost2_id", nullable = false)
    var kost2: Kost2DO? = null

    @PropertyInfo(i18nKey = "comment")
    @Field
    @get:Column(length = 4000)
    var comment: String? = null

    /**
     * In form yyyy-mm-###
     */
    val formattedSatzNummer: String
        @Transient
        get() = year.toString() + '-'.toString() + StringHelper.format2DigitNumber(month!! + 1) + '-'.toString() + formatSatzNr()

    val kontoId: Int?
        @Transient
        get() = if (this.konto != null) this.konto!!.id else null

    val gegenKontoId: Int?
        @Transient
        get() = if (this.gegenKonto != null) this.gegenKonto!!.id else null

    val kost1Id: Int?
        @Transient
        get() = if (this.kost1 != null) this.kost1!!.id else null

    val kost2Id: Int?
        @Transient
        get() = if (this.kost2 != null) this.kost2!!.id else null

    fun formatSatzNr(): String? {
        return if (satznr == null) {
            ""
        } else StringUtils.leftPad(satznr.toString(), 5, '0')
    }

    /**
     * Führt nach der Datev-/Steffi-Logik Betrachtungen durch, ob dieser Datensatz berücksichtigt werden muss bzw. ob die
     * Betrag im Haben oder im Soll anzuwenden ist.
     */
    fun calculate() {
        if (konto == null || kost2 == null) {
            log.warn(
                    "Can't calculate Buchungssatz, because konto or kost2 is not given (for import it will be detected, OK): $this")
            return
        }
        val kto = konto!!.nummer!!
        val sollHaben = sh
        if (kto >= 4400 && kto <= 4499) {
            // Konto 4400 - 4499 werden im Haben gebucht: Umsatz.
        }
        if (kto >= 5900 && kto <= 5999) {
            // Fremdleistungen
        }
        if (sollHaben == SHType.SOLL) {
            betrag = betrag!!.negate()
        }
        if (kost2!!.isEqual(1, 0, 0, 0) == true && kto >= 6000 && kto <= 6299) { // "1.000.00.00"
            // Bei diesen Buchungen handelt es sich um Kontrollbuchungen mit dem Gegenkonto 3790, was wir hier nochmals prüfen:
            if (gegenKonto!!.nummer != 3790) {
                // log.error("Bei dieser Buchung ist das Gegenkonto nicht 3790, wie von der Buchhaltung mitgeteilt "
                // + "(deshalb wird dieser Datensatz nicht ignoriert!");
            } else {
                isIgnore = true
            }
        }
    }

    fun setSH(value: String) {
        if ("S" == value) {
            sh = SHType.SOLL
        } else if ("H" == value) {
            sh = SHType.HABEN
        } else {
            val msg = "Haben / Soll-Wert ist undefiniert: $this"
            log.error(msg)
            throw RuntimeException(msg)
        }
    }

    override fun compareTo(other: BuchungssatzDO): Int {
        var r = this.year!!.compareTo(other.year!!)
        if (r != 0) {
            return r
        }
        r = this.month!!.compareTo(other.month!!)
        return if (r != 0) {
            r
        } else this.satznr!!.compareTo(other.satznr!!)
    }
}