package org.projectforge.rest.ui

import org.projectforge.business.book.BookDO
import org.projectforge.business.book.BookFilter
import org.projectforge.business.book.BookStatus
import org.projectforge.business.book.BookType
import org.projectforge.ui.*

/**
 * maxLength is automatically set by @Column(length=...) of JPA definition.
 * label = "@" will be replaced by @PropertyColumn setting in BookDO.
 */
class BookLayout {
    companion object {
        fun createListLayout(): UILayout {
            val layout = UILayout("book.title.list")
                    .add(UITable("result-set")
                            .add(UITableColumn("created", dataType = UIDataType.DATE, formatter = Formatter.TIMESTAMP_MINUTES))
                            .add("yearOfPublishing", "signature", "authors", "title", "keywords")
                            .add(UITableColumn("lendOutBy", formatter = Formatter.USER)))
            LayoutUtils.addListFilterContainer(layout,
                    UICheckbox("present"), UICheckbox("missed"), UICheckbox("disposed"),
                    filterClass = BookFilter::class.java)
            return LayoutUtils.processListPage(layout, BookDO::class.java)
        }

        fun createEditLayout(book: BookDO?): UILayout {
            val titleKey = if (book?.id != null) "book.title.edit" else "book.title.add"
            val layout = UILayout(titleKey)
                    .add(UIGroup().add(UIInput("title", required = true, focus = true)))
                    .add(UIGroup().add(UIInput("authors")))
                    .add(UIRow()
                            .add(UICol(6)
                                    .add(UIGroup().add(UISelect("type", i18nEnum = BookType::class.java, required = true)))
                                    .add("yearOfPublishing")
                                    .add(UIGroup().add(UISelect("status", i18nEnum = BookStatus::class.java, required = true)))
                                    .add("signature"))
                            .add(UICol(6)
                                    .add("isbn", "keywords", "publisher", "editor")))
                    .add(UIGroup()
                            .add(UILabel("book.lending"))
                            .add(UICustomized("lendOutComponent")))
                    .add(UIGroup().add(UITextarea("lendOutComment")))
                    .add(UIGroup().add(UITextarea("abstractText")))
                    .add(UIGroup().add(UITextarea("comment")))
            return LayoutUtils.processEditPage(layout, BookDO::class.java, book)
        }
    }
}
