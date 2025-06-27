package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import grails.web.api.WebAttributes
import jakarta.annotation.PostConstruct
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.runtime.MethodClosure as MC
import taack.ast.annotation.TaackFieldEnum
import taack.ast.type.FieldInfo
import taack.ast.type.GetMethodReturn
import taack.domain.TaackFilterService
import taack.ui.dsl.UiFilterSpecifier
import taack.ui.dsl.UiFormSpecifier
import taack.ui.dsl.UiMenuSpecifier
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.ui.dsl.UiTableSpecifier
import taack.ui.dsl.filter.expression.FilterExpression
import taack.domain.TaackFilter
import taack.ui.dsl.common.ActionIcon
import taack.ui.dsl.common.IconStyle

import javax.swing.Icon
import java.lang.reflect.Field
import static taack.render.TaackUiService.tr
import taack.ui.dsl.filter.expression.Operator

/**
 * UI Service responsible for constructing all UI components (menus, tables, filters, forms)
 * related to *MyLibrary* domain objects (Authors, Books, Book Instances). This class delegates
 * rendering to the Taack framework by returning specifiers for each UI block.
 *
 * <p>
 * **Main responsibilities:**
 * <ul>
 *   <li>Builds menus for navigating the library system</li>
 *   <li>Constructs filters for searching and narrowing down entities</li>
 *   <li>Generates tables for displaying lists of authors, books, and book instances</li>
 *   <li>Creates forms for creating or editing authors and books</li>
 *   <li>Provides purchase forms for creating multiple book instances</li>
 * </ul>
 *
 * The UI components are used by {@link MyLibraryController} to handle user interactions.
 *
 * All functions are written in Groovy/Grails idiomatic style using `def` or explicit return types for clarity.
 */

@GrailsCompileStatic
@Secured(['ROLE_ADMIN'])
class MyLibraryUiService implements WebAttributes {
    TaackFilterService taackFilterService

    static lazyInit = false
    /**
     * Initializes the service by registering this app's icon and default entry point
     * (the controller index action) with the Taack framework.
     *
     * **How it works:**
     * - Loads an SVG icon file from resources.
     * - Calls TaackAppRegisterService.register with the icon and entry point.
     */
    @PostConstruct
    void init() {
        //TODO chose picture
        TaackAppRegisterService.register(new TaackApp(MyLibraryController.&index as MC, new String(this.class.getResourceAsStream("/myLibrary/library-svgrepo-com.svg").readAllBytes())))
    }

    /*------------------------------------------------------------*/
    /* General Menu                                               */
    /*------------------------------------------------------------*/

    /**
     * Builds the general navigation menu for the library app.
     *
     * **Returns:** A UiMenuSpecifier containing:
     * - A menu item linking to the list of books.
     * - A menu item linking to the main index page.
     *
     * **Purpose:** Allows users to navigate between main screens of the application.
     */
    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu MyLibraryController.&listBook as MC
            menu MyLibraryController.&index as MC
        }
    }

    /*------------------------------------------------------------*/
    /* Author Menu                                                */
    /*------------------------------------------------------------*/

    /**
     * Builds a filter specifier that allows filtering authors based on whether they are active.
     *
     * **Parameters:**
     * - author: An instance of MyLibraryAuthor used to reference the isActive field.
     *
     * **Returns:** A UiFilterSpecifier containing:
     * - A section titled "Filter".
     * - A boolean filter field labeled "Is Active" comparing the author's isActive property to true.
     */
    UiFilterSpecifier buildIsActiveAuthorFilter(MyLibraryAuthor author) {
        UiFilterSpecifier isActiveAuthorFilter = new UiFilterSpecifier()
        isActiveAuthorFilter.ui MyLibraryAuthor, {
            section "Filter", {
                filterFieldExpressionBool "Is Active", new FilterExpression(true, Operator.EQ, author.isActive_)
            }
        }
    }

    /**
     * Builds a table displaying authors with their first name, last name, active status,
     * and action buttons for viewing, deleting, or reactivating.
     *
     * **Parameters:**
     * - isSelect: If true, builds the table for selection mode (for example, when choosing an author for a book form).
     *
     * **Returns:** A UiTableSpecifier with:
     * - A header including first name and last name (and isActive status if not in select mode).
     * - For each author:
     *   - Displays their first and last name.
     *   - Adds a link action to show author details.
     *   - In select mode, adds a SELECT action to pick the author.
     *   - In normal mode, adds DELETE and ACTIVATE buttons to deactivate or reactivate authors.
     *
     * **Use case:** Called by controllers to render the main author list or selection modals.
     */
    UiTableSpecifier buildAuthorTable(Boolean isSelect = false) {

        MyLibraryAuthor author = new MyLibraryAuthor()
        UiTableSpecifier authorTableSpecifier = new UiTableSpecifier()

        authorTableSpecifier.ui {
            header {
                column {label author.firstName_}
                label author.lastName_
                if(!isSelect) {
                    label author.isActive_
                    label "Delete Author"
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryAuthor)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, author.lastName_)

            if(isSelect) {
                filter.addFilter(buildIsActiveAuthorFilter(author)) //only displays the authors active for the from to select an author for the books)
            }
            iterate(
                    filter.build()) { MyLibraryAuthor authorIterator ->
                rowColumn {
                    rowAction authorIterator.firstName, MyLibraryController.&showAuthor as MC, authorIterator.id //put the link
                    if (isSelect) {rowAction tr('default.role.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, authorIterator.id, authorIterator.toString()}
                }
                rowField authorIterator.lastName_
                if(!isSelect) {
                    rowField authorIterator.isActive_ //for borrower remove
                    rowColumn {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteAuthor as MC, authorIterator.id
                        rowAction ActionIcon.CREATE * IconStyle.SCALE_DOWN, MyLibraryController.&activateAuthor as MC, authorIterator.id
                    }
                }
            }
        }
    }

    /**
     * Builds a form for creating or editing an author.
     *
     * **Parameters:**
     * - author: Optional existing author to edit; if null, creates a new author object using request parameters.
     *
     * **Returns:** A UiFormSpecifier with:
     * - Fields for first name, last name, date of birth, and active status.
     * - A form action linking to MyLibraryController.saveAuthor to handle form submission.
     *
     * **Use case:** Rendered in modals for author creation or editing.
     */
    UiFormSpecifier buildAuthorForm(MyLibraryAuthor author) {
        author ?= new MyLibraryAuthor(params)
        UiFormSpecifier createAuthorSpecifier = new UiFormSpecifier()
        createAuthorSpecifier.ui author, {
            section "Author details", {
                field author.firstName_
                field author.lastName_
                field author.dateOfBirth_
                field author.isActive_
            }
            formAction MyLibraryController.&saveAuthor as MC
        }
    }

    /**
     * Builds a filter specifier to filter authors by their last name.
     *
     * **Returns:** A UiFilterSpecifier with:
     * - A section titled "Author Filter".
     * - A text filter field for the author's last name.
     *
     * **Use case:** Used on author list screens to narrow down results by name.
     */
    UiFilterSpecifier buildAuthorFilter() {
        MyLibraryAuthor author = new MyLibraryAuthor()
        UiFilterSpecifier authorFilterSpecifier = new UiFilterSpecifier()

        authorFilterSpecifier.ui MyLibraryAuthor, {
            section "Author Filter", {
                filterField author.lastName_
            }
        }
    }

    /*------------------------------------------------------------*/
    /* Book Menu                                                  */
    /*------------------------------------------------------------*/

    /**
     * Builds the table displaying books with their title, author (optional), number of instances,
     * number of available instances, and action buttons.
     *
     * **Parameters:**
     * - author: Optional author to restrict the displayed books (shows only books by this author).
     *
     * **Returns:** A UiTableSpecifier with:
     * - A header with columns for title, author, number of instances, and actions.
     * - For each book:
     *   - Displays title and author.
     *   - Shows number of total instances and number available.
     *   - Includes SHOW, EDIT, DELETE INSTANCE and ADD INSTANCE actions.
     *
     * **Use case:** The main book list table.
     */
    UiTableSpecifier buildBookTable(MyLibraryAuthor author = null) {
        MyLibraryBook book = new MyLibraryBook()
        UiTableSpecifier bookTableSpecifier = new UiTableSpecifier()
        bookTableSpecifier.ui {
            header {
                column {label book.title_}
                if (!author) {sortableFieldHeader book.author_}
                column {
                    label "Number of instances "//book.numberOfInstances
                }
                if (!author) {
                    column {
                        label "Modify number of Book Instances" //only for ADMIN
                    }
                }
            }
            TaackFilter.FilterBuilder filter =  taackFilterService.getBuilder(MyLibraryBook)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, book.title_)
            if(author) {
                filter.addRestrictedIds(author.listOfBooks*.id as Long[]) //book.author = author
            }
            iterate(
                    filter.build()) { MyLibraryBook bookIterator ->
                rowColumn {
                    rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, MyLibraryController.&showBook as MC, bookIterator.id
                    rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, MyLibraryController.&createBook as MC, bookIterator.id
                    rowField bookIterator.title_
                }
                if (!author) {rowField bookIterator.author_}
                rowColumn {
                    bookIterator.getNumberOfInstances()
                    rowField bookIterator.numberOfInstances_  //for borrower remove
                }
                if (!author) {
                    rowColumn {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&selectBookInstance as MC, bookIterator.id
                        rowAction ActionIcon.ADD * IconStyle.SCALE_DOWN, MyLibraryController.&purchaseBook as MC, bookIterator.id
                    }
                }
            }
        }
    }

    /**
     * Builds a form for creating or editing a book.
     *
     * **Parameters:**
     * - book: Optional existing book to edit; if null, creates a new book object using request parameters.
     *
     * **Returns:** A UiFormSpecifier with:
     * - Fields for title, author (ajax field selector), number of pages, and description.
     * - A form action linking to MyLibraryController.saveBook.
     *
     * **Use case:** Rendered in modals for book creation or editing.
     */
    UiFormSpecifier buildBookForm(MyLibraryBook book) {
        book ?= new MyLibraryBook(params)
        UiFormSpecifier bookFormSpecifier = new UiFormSpecifier()
        bookFormSpecifier.ui book, {
            section "Author details", {
                field book.title_
                ajaxField book.author_, MyLibraryController.&selectAuthor as MC
                field book.numberOfPages_
                field book.description_
            }
            formAction MyLibraryController.&saveBook as MC
        }
    }

    /**
     * Builds a filter specifier for filtering books by their title.
     *
     * **Returns:** A UiFilterSpecifier with:
     * - A section titled "Book Filter".
     * - A text filter field for the book title.
     *
     * **Use case:** Used in book list views to narrow down results by title.
     */
    UiFilterSpecifier buildBookFilter() {
        MyLibraryBook book = new MyLibraryBook()
        UiFilterSpecifier bookFilterSpecifier = new UiFilterSpecifier()

        bookFilterSpecifier.ui MyLibraryBook, {
            section "Book Filter", {
                filterField book.title_
            }
        }
    }

    /**
     * Builds a filter to check if book instances are active.
     *
     * **Parameters:**
     * - book: The book whose instances to filter.
     *
     * **Returns:** A UiFilterSpecifier with:
     * - A section titled "Filter".
     * - A boolean filter field labeled "Is Active", comparing the isActive status of instances to true.
     *
     * **Use case:** Used in book management screens to show only active copies.
     */
    UiFilterSpecifier buildIsActiveBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isActive_)
        }
    }

    /**
     * Builds a form to specify the number of physical copies (book instances) to purchase for a book.
     *
     * **Parameters:**
     * - book: The book for which to purchase instances.
     *
     * **Returns:** A UiFormSpecifier with:
     * - A numeric field for "Number of Instances".
     * - A form action linking to MyLibraryController.purchaseAndSaveBook to handle the purchase.
     *
     * **Use case:** Rendered in modals for librarians to add copies to library inventory.
     */
    UiFormSpecifier buildBookPurchase(MyLibraryBook book) {
        NumberForInstances numberForInstances = new NumberForInstances()
        book ?= new MyLibraryBook(params)
        UiFormSpecifier bookPurchaseSpecifier = new UiFormSpecifier()
        bookPurchaseSpecifier.ui book, {
            section "Purchase Number", {
                field numberForInstances.numberOfInstances_
            }
            formAction MyLibraryController.&purchaseAndSaveBook as MC
        }
    }

    /**
     * Builds a table displaying all physical instances of a given book.
     *
     * **Parameters:**
     * - book: The book whose instances to display.
     * - isOne: If true, enables selection mode (e.g. for lending). If false, shows delete actions.
     * - bookInstance: Optional instance for context.
     *
     * **Returns:** A UiTableSpecifier with:
     * - A header showing "Serial Number" and action columns (Select or Delete).
     * - For each book instance:
     *   - Displays the serial number.
     *   - Shows SELECT or DELETE action depending on `isOne`.
     *
     * **Use case:** Used to manage copies of a book or select one for borrowing.
     */
    UiTableSpecifier buildInstanceBookTable(MyLibraryBook book, isOne = false, MyLibraryBookInstance bookInstance = null) {
        UiTableSpecifier table = new UiTableSpecifier()
        bookInstance ?= new MyLibraryBookInstance()
        table.ui {
            header {
                label "Serial Number"
                if(isOne) {
                    column {
                        label "Select Book Instance"
                    }
                }
                if(!isOne) {
                    column {
                        label "Delete"
                    }
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryBookInstance).addRestrictedIds(book.listOfBookInstance*.id as Long[])
            filter.addFilter(buildIsActiveBookInstances(book))

            iterate(
                    filter.build()) { MyLibraryBookInstance bookInstanceIterator ->
                rowField bookInstanceIterator.serialNumber_
                rowColumn {
                    if(!isOne) {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteBookInstances as MC, bookInstanceIterator.id, [bookId:book.id]
                    } else {
                        rowAction tr('default.serialNumber.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, bookInstanceIterator.id, bookInstanceIterator.serialNumber.toString()
                    }
                }
            }
        }
    }



}

