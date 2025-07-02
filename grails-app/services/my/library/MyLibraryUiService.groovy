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
import taack.ui.dsl.UiShowSpecifier
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
        //TODO chose icon of your choice and add the icon to app/myLibrary/src/resources/myLibrary
        TaackAppRegisterService.register(new TaackApp(MyLibraryController.&index as MC, new String(this.class.getResourceAsStream("/myLibrary/library-svgrepo-com.svg").readAllBytes())))
    }

    /*------------------------------------------------------------*/
    /* General Menu                                               */
    /*------------------------------------------------------------*/

    /**
     * Builds and returns the main navigation menu for the library app.
     *
     * Adds two menu items:
     * - One linking to MyLibraryController.listBook (books list view)
     * - One linking to MyLibraryController.index (home page)
     *
     * **Purpose:** Allows users to navigate between main screens of the application.
     */
    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu MyLibraryController.&listBook as MC
            menu MyLibraryController.&index as MC

            //ADDED
            menu MyLibraryController.&listBooksBorrowed as MC
            menu MyLibraryController.&listBooksCurrentlyBorrowed as MC
        }
    }

    /*------------------------------------------------------------*/
    /* Author Menu                                                */
    /*------------------------------------------------------------*/

    /**
     * Builds a filter specifier to filter authors based on their active status.
     *
     * **Purpose:** Allows users to filter the author list by whether they are currently active.
     *
     * **How it works (to implement):**
     * - Create a new `UiFilterSpecifier` for `MyLibraryAuthor`.
     * - Define a UI block with:
     *   - A section titled "Filter".
     *   - Inside the section, add a boolean filter field labeled "Is Active" that uses a `FilterExpression`
     *     to compare the author's `isActive_` field to `true` using the EQ (equals) operator.
     *
     * **Inputs:**
     * - `author`: An instance of `MyLibraryAuthor` to reference its fields in the filter.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` configured to filter authors by active status.
     *
     * **Example implementation:*
     * filterFieldExpressionBool "NameOfFilter", new FilterExpression(Boolean, Operator, FieldInfo)
     *
     * **Tip:** Remember to import or reference `Operator.EQ` for equality comparison.
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
     * Builds a filter specifier for filtering authors by their last name.
     *
     * **Purpose:** Allows users to narrow down the list of authors based on their last name input.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryAuthor` to access its fields.
     * - Create a new `UiFilterSpecifier` for `MyLibraryAuthor`.
     * - Define a UI block with:
     *   - A section titled "Author Filter".
     *   - Inside the section, add a filter field for the author's last name.
     *
     * **Inputs:** None directly; creates an author instance for field references.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` configured to filter authors by last name.
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

    /**
     * Builds a table displaying authors with their first name, last name, active status (if not in select mode),
     * and action buttons to view, select, delete, or activate authors.
     *
     * **Purpose:** Generates the author listing table for both normal viewing and selection modals.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryAuthor` to access its fields.
     * - Create a new `UiTableSpecifier` to define the table structure.
     * - In the table header:
     *   - Add a column for first name using `label author.firstName_`.
     *   - Add a column for last name using `label author.lastName_`.
     *   - If `isSelect` is false, add columns for `isActive_` and "Delete Author" actions.
     * - Build a `TaackFilter.FilterBuilder` for `MyLibraryAuthor`:
     *   - Set a max of 10 lines per page.
     *   - Sort results by last name in ascending order.
     *   - If `isSelect` is true, add a filter to show only active authors by calling `buildIsActiveAuthorFilter(author)`.
     * - Use `iterate(filter.build())` to iterate through each author result:
     *   - In each row:
     *     - Add a rowAction linking the author's first name to the `showAuthor` action in the controller.
     *     - If `isSelect` is true, add a SELECT action icon to select the author (using ActionIcon.SELECT).
     *     - Add the author's last name as a field.
     *     - If not `isSelect`:
     *       - Add the author's `isActive_` status as a field.
     *       - Add DELETE and ACTIVATE action icons linked to `deleteAuthor` and `activateAuthor` controller actions.
     *
     * **Inputs:**
     * - `isSelect`: Boolean flag to indicate if the table is used for selection (true) or normal listing (false).
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the author table with appropriate columns and actions.
     *
     * **Tips:**
     * - Ensure correct use of `rowAction` syntax to link actions and display labels.
     * - Remember to cast controller method references with `as MC` when used as closures in actions.
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
                filter.addFilter(buildIsActiveAuthorFilter(author))
            }
            iterate(
                    filter.build()) { MyLibraryAuthor authorIterator ->
                rowColumn {
                    rowAction authorIterator.firstName, MyLibraryController.&showAuthor as MC, authorIterator.id
                    if (isSelect) {
                        rowAction tr('default.role.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, authorIterator.id, authorIterator.toString()
                    }
                }
                rowField authorIterator.lastName_
                if(!isSelect) {
                    rowField authorIterator.isActive_
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
     * **Purpose:** Generates the UI form allowing users to input or update author details.
     *
     * **How it works (to implement):**
     * - Check if `author` is null; if so, create a new `MyLibraryAuthor` using request parameters.
     * - Create a new `UiFormSpecifier` to define the form layout.
     * - Define a UI block for the form:
     *   - Add a section titled "Author details".
     *   - Inside the section, add fields for:
     *     - firstName
     *     - lastName
     *     - dateOfBirth
     *     - isActive
     *   - Define a form action linking to `MyLibraryController.saveAuthor` to handle form submission.
     *
     * **Inputs:**
     * - `author`: Optional existing author to edit; if null, initializes a new author.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the author form for creation or editing.
     *
     * Adds a form field for the author's first name.
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
     * Builds a read-only detail view specifier for displaying author information.
     *
     * **Purpose:** Generates a UI specifier to show the author's details in a non-editable format.
     *
     * **How it works (to implement):**
     * - Create a new `UiShowSpecifier` instance.
     * - Define a UI block for the given `author` that displays:
     *   - firstName
     *   - lastName
     *   - dateOfBirth
     *   - isActive status
     *   using `fieldLabeled` for each field to include labels in the display.
     *
     * **Inputs:**
     * - `author`: The author whose details are to be shown.
     *
     * **Outputs:** Returns a `UiShowSpecifier` configured to display the author's details.
     */
    UiShowSpecifier buildAuthorShow(MyLibraryAuthor author) {
        UiShowSpecifier authorShowSpecifier = new UiShowSpecifier()

        authorShowSpecifier.ui(author, {
            fieldLabeled author.firstName_
            fieldLabeled author.lastName_
            fieldLabeled author.dateOfBirth_
            fieldLabeled author.isActive_
        })
    }

    /*------------------------------------------------------------*/
    /* Book Menu                                                  */
    /*------------------------------------------------------------*/

    /**
     * Builds the table displaying books with their title, author (if no specific author is provided),
     * number of instances, and action buttons for viewing, editing, and managing book instances.
     *
     * **Purpose:** Generates the book listing table for either all books or books by a specific author.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryBook`.
     * - Create a new `UiTableSpecifier` for displaying book data.
     * - Define the table UI with:
     *   - A header containing title, author (conditional), number of instances, and modify instances columns.
     * - Build a filter using `taackFilterService.getBuilder`, configure sorting and optional author restriction.
     * - Iterate through results to display each book with actions for SHOW, EDIT, DELETE, and ADD.
     *
     * **Inputs:**
     * - `author`: Optional. If provided, displays only books by this author.
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the book table with appropriate columns and actions.
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
                    //ADDED LINE
                    label "Number of Available Book Instances"

                    column {
                        label "Modify number of Book Instances"
                    }
                    //ADDED column
                    column {
                        label "Request Form"
                    }
                }
            }
            TaackFilter.FilterBuilder filter =  taackFilterService.getBuilder(MyLibraryBook)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, book.title_)

            //ADDED LINE
            filter.addFilter(buildIsAvailableBookFilter(book))

            if(author) {
                filter.addRestrictedIds(author.listOfBooks*.id as Long[])
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
                    rowField bookIterator.numberOfInstances_
                }
                if (!author) {
                    //ADDED LINE
                    rowField bookIterator.numberOfBooksBorrowable_

                    rowColumn {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&selectBookInstance as MC, bookIterator.id
                        rowAction ActionIcon.ADD * IconStyle.SCALE_DOWN, MyLibraryController.&purchaseBook as MC, bookIterator.id
                    }

                    //ADDED rowColumn
                    rowColumn {
                        rowAction ActionIcon.CREATE * IconStyle.SCALE_DOWN, MyLibraryController.&requestBookInstance as MC, bookIterator.id
                    }
                }
            }
        }
    }

    /**
     * Builds a form for creating or editing a book.
     *
     * **Purpose:** Generates the UI form allowing users to input or update book details.
     *
     * **How it works (to implement):**
     * - If `book` is null, initialize it using `new MyLibraryBook(params)`.
     * - Create a new `UiFormSpecifier` for the book form.
     * - Define a UI block with:
     *   - A section titled "Author details".
     *   - Inside the section, add fields for:
     *     - title
     *     - author (ajax field selector linked to selectAuthor)
     *     - number of pages
     *     - description
     *   - Define formAction linking to `MyLibraryController.saveBook`.
     *
     * **Inputs:**
     * - `book`: Optional existing book to edit; if null, initializes a new book.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the book form for creation or editing.
     */
    UiFormSpecifier buildBookForm(MyLibraryBook book) {
        book ?= new MyLibraryBook(params)
        UiFormSpecifier bookFormSpecifier = new UiFormSpecifier()
        bookFormSpecifier.ui book, {
            section "Book details", {
                field book.title_
                ajaxField book.author_, MyLibraryController.&selectAuthor as MC //(1)
                field book.numberOfPages_
                field book.description_
            }
            formAction MyLibraryController.&saveBook as MC
        }
    }

    /**
     * Builds a filter specifier for filtering books by their title.
     *
     * **Purpose:** Allows users to narrow down the list of books based on their title input.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryBook`.
     * - Create a new `UiFilterSpecifier` for `MyLibraryBook`.
     * - Define a UI block with:
     *   - A section titled "Book Filter".
     *   - Inside the section, add a filter field for the book's title.
     *
     * **Inputs:** None directly; creates a book instance for field references.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` configured to filter books by title.
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
     * Builds a form for specifying the number of physical copies (book instances) to purchase for a book.
     *
     * **Purpose:** Allows librarians to input the quantity of book instances to add to inventory.
     *
     * **How it works (to implement):**
     * - Create a new instance of `NumberForInstances`.
     * - If `book` is null, initialize it using `new MyLibraryBook(params)`.
     * - Create a new `UiFormSpecifier` for the purchase form.
     * - Define a UI block with:
     *   - A section titled "Purchase Number".
     *   - Inside the section, add a field for `numberForInstances.numberOfInstances_`.
     *   - Define formAction linking to `MyLibraryController.purchaseAndSaveBook`.
     *
     * **Inputs:**
     * - `book`: The book for which to purchase instances.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the purchase form.
     */
    UiFormSpecifier buildBookPurchase(MyLibraryBook book) {
        NumberForInstances numberForInstances = new NumberForInstances()
        book ?= new MyLibraryBook(params)
        UiFormSpecifier bookPurchaseSpecifier = new UiFormSpecifier()
        bookPurchaseSpecifier.ui book, {
            section "Purchase Number", {
                field numberForInstances.numberOfInstances_ //<1>
            }
            formAction MyLibraryController.&purchaseAndSaveBook as MC
        }
    }

    /**
     * Builds a read-only detail view specifier for displaying book information.
     *
     * **Purpose:** Generates a UI specifier to show the book's details in a non-editable format.
     *
     * **How it works (to implement):**
     * - Create a new `UiShowSpecifier` for the book.
     * - Define a UI block displaying the following fields using `fieldLabeled`:
     *   - title
     *   - author
     *   - number of pages
     *   - description
     *   - number of instances
     *
     * **Inputs:**
     * - `book`: The book whose details are to be shown.
     *
     * **Outputs:** Returns a `UiShowSpecifier` configured to display the book's details.
     */
    UiShowSpecifier buildBookShow(MyLibraryBook book) {
        UiShowSpecifier bookShowSpecifier = new UiShowSpecifier()

        bookShowSpecifier.ui(book, {
            fieldLabeled book.title_
            fieldLabeled book.author_
            fieldLabeled book.numberOfPages_
            fieldLabeled book.description_
            fieldLabeled book.numberOfInstances_
        })
    }

    /**
     * Builds a filter specifier to select only active book instances for a given book.
     *
     * **Purpose:** Allows filtering of book instances based on their active status.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryBookInstance`.
     * - Create a new `UiFilterSpecifier` for `MyLibraryBookInstance`.
     * - Define a UI block with:
     *   - A boolean filter field expression checking if `isActive_` is true using `FilterExpression` and `Operator.EQ`.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be filtered.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` to filter active book instances.
     */
    UiFilterSpecifier buildIsActiveBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isActive_)
        }
    }
    /**
     * Builds a table displaying all active physical instances of a given book.
     *
     * **Purpose:** Allows librarians to view and manage individual book instances, such as deleting specific copies.
     *
     * **How it works (to implement):**
     * - Create a new `UiTableSpecifier`.
     * - Define a UI block with:
     *   - A header containing:
     *     - A label for "Serial Number".
     *     - A column with a "Delete" label.
     * - Build a filter for `MyLibraryBookInstance`:
     *   - Restrict IDs to instances belonging to the given book.
     *   - Add a filter to select only active book instances.
     * - Iterate through filtered results:
     *   - Display the serial number field.
     *   - Add a DELETE action icon linked to `MyLibraryController.deleteBookInstances`, passing the instance ID and the book ID.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be displayed.
     * - `bookInstance`: Optional specific instance context (default null).
     *
     * **Outputs:** Returns a `UiTableSpecifier` configured to display and manage book instances.
     */
    UiTableSpecifier buildInstanceBookTable(MyLibraryBook book, isOne = false, MyLibraryBookInstance bookInstance = null) {
        UiTableSpecifier table = new UiTableSpecifier()
        table.ui {
            header {
                label "Serial Number"

                //ADDED the isOne variable and everything except the column and delete
                column {
                    if (isOne) {
                        label "Select Book Instance"
                    } else {
                        label "Delete"
                    }
                }


            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryBookInstance).addRestrictedIds(book.listOfBookInstance*.id as Long[])
            filter.addFilter(buildIsActiveBookInstances(book))

            //ADDED LINE
            filter.addFilter(buildIsAvailableBookInstances(book))

            iterate(
                    filter.build()) { MyLibraryBookInstance bookInstanceIterator ->
                rowField bookInstanceIterator.serialNumber_

                rowColumn {
                    // ADDED everything except the the deleteAction
                    if(isOne) {
                        rowAction tr('default.serialNumber.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, bookInstanceIterator.id, bookInstanceIterator.serialNumber.toString()
                    } else {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteBookInstances as MC, bookInstanceIterator.id, [bookId:book.id]
                    }
                }
            }
        }
    }

    //ADDED
    UiFilterSpecifier buildIsAvailableBookFilter(MyLibraryBook book) {
        UiFilterSpecifier isAvailableBookFilter = new UiFilterSpecifier()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        isAvailableBookFilter.ui MyLibraryBook, {
            section "Filter", {
                filterFieldExpressionBool "Is Available", new FilterExpression(true, Operator.EQ, book.listOfBookInstance_,bookInstance.isAvailableB_)
            }
        }
    }

    //ADDED BLOCK
    UiFilterSpecifier buildIsAvailableBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isAvailableB_)    //new FilterExpression(true, Operator.EQ, bookInstance.isAvailable_)
        }
    }

    //ADDED BLOCK
    UiFormSpecifier buildRequestBookForm(MyLibraryBook book) {
        User user = springSecurityService.currentUser as User
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        borrowed.user = user
        book ?= new MyLibraryBook(params)
        UiFormSpecifier requestBookFormSpecifier = new UiFormSpecifier()

        requestBookFormSpecifier.ui borrowed, {
            section "Request Book Form", {
                hiddenField borrowed.user_ //pass paramretes in the form witout the user seeing
                field borrowed.requestDate_
                ajaxField borrowed.bookInstance_, MyLibraryController.&selectBookInstanceOne as MC, book.id

            }
            formAction MyLibraryController.&requestBookForm as MC
        }
    }




}



