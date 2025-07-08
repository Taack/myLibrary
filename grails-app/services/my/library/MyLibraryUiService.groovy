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
    SpringSecurityService springSecurityService

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

    /**
     * Checks if the current user has the ROLE_ADMIN authority.
     *
     * **Purpose:** Determines whether the currently logged-in user has administrative privileges within the system.
     *
     * **How it works (to implement):**
     * - Retrieves the current user from `springSecurityService.currentUser` and casts it to `User`.
     * - Returns true if the user's authorities include `ROLE_ADMIN`; otherwise, returns false.
     *
     * **Inputs:** None directly; relies on the authenticated current user.
     *
     * **Outputs:** Returns a boolean indicating if the user is an admin.
     */
    boolean isAdmin() {
        // TODO 3.1.1: Retrieve currentUser from springSecurityService.currentUser and cast to User.
        // TODO 3.1.2: Return true if currentUser.authorities contains 'ROLE_ADMIN'; else return false.

        // remove the following line after implementation
        return true
    }

    /*------------------------------------------------------------*/
    /* General Menu                                               */
    /*------------------------------------------------------------*/

    /**
     * Builds the main menu for the library UI.
     *
     * **Purpose:** Provides navigation options for the library system, including:
     * - Viewing the list of books.
     * - Accessing the home/index page.
     * - Viewing the list of borrowed books.
     * - Viewing the list of currently borrowed books.
     * - Managing users.
     * - Viewing borrow requests.
     *
     * **How it works:**
     * - Creates a new `UiMenuSpecifier`.
     * - Defines a UI block adding menu items linked to corresponding controller actions.
     *
     * **Outputs:** Returns a `UiMenuSpecifier` defining the full menu structure for the library UI.
     */
    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {
            menu MyLibraryController.&listBook as MC
            menu MyLibraryController.&index as MC
            menu MyLibraryController.&listBooksBorrowed as MC
            menu MyLibraryController.&listBooksCurrentlyBorrowed as MC

            // TODO 2.1: Add menu entry for MyLibraryController.listOfUsers.
            // TODO 2.2: Add menu entry for MyLibraryController.listOfRequests.
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
     * Builds a table displaying authors with their first name, last name, active status (if not in select mode and if user is admin),
     * and action buttons to view, select, delete, or activate authors.
     *
     * **Purpose:** Generates the author listing table for both normal viewing and selection modals, with additional management options available to admins.
     *
     * **How it works (to implement):**
     * - Determine if the current user is an admin by calling `isAdmin()`.
     * - Create a new instance of `MyLibraryAuthor` to access its fields.
     * - Create a new `UiTableSpecifier` to define the table structure.
     * - In the table header:
     *   - Add a column for first name using `label author.firstName_`.
     *   - Add a column for last name using `label author.lastName_`.
     *   - If `isSelect` is false and the user is an admin, add columns for `isActive_` and "Delete Author" actions.
     * - Build a `TaackFilter.FilterBuilder` for `MyLibraryAuthor`:
     *   - Set a max of 10 lines per page.
     *   - Sort results by last name in ascending order.
     *   - If the user is not an admin, add a filter to show only active authors by calling `buildIsActiveAuthorFilter(author)`.
     *   - If `isSelect` is true, also add a filter to show only active authors.
     * - Use `iterate(filter.build())` to iterate through each author result:
     *   - In each row:
     *     - Add a rowAction linking the author's first name to the `showAuthor` action in the controller.
     *     - If `isSelect` is true, add a SELECT action icon to select the author (using ActionIcon.SELECT).
     *     - Add the author's last name as a field.
     *     - If not `isSelect` and the user is an admin:
     *       - Add the author's `isActive_` status as a field.
     *       - Add DELETE and ACTIVATE action icons linked to `deleteAuthor` and `activateAuthor` controller actions.
     *
     * **Inputs:**
     * - `isSelect`: Boolean flag to indicate if the table is used for selection (true) or normal listing (false).
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the author table with appropriate columns and actions.
     *
     */
    UiTableSpecifier buildAuthorTable(Boolean isSelect = false) {
        // TODO 4.1.1: Determine if the current user is an admin by calling isAdmin() and store result in isAdmin.

        MyLibraryAuthor author = new MyLibraryAuthor()
        UiTableSpecifier authorTableSpecifier = new UiTableSpecifier()

        authorTableSpecifier.ui {
            header {
                column {label author.firstName_}
                label author.lastName_
                if(!isSelect) { // TODO 4.1.2: Add && isAdmin condition to display admin columns only for admins.
                    label author.isActive_
                    label "Delete Author"
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryAuthor)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, author.lastName_)

            // TODO 4.1.3: If user is not admin, add filter buildIsActiveAuthorFilter.

            if(isSelect) {filter.addFilter(buildIsActiveAuthorFilter(author))}
            iterate(
                    filter.build()) { MyLibraryAuthor authorIterator ->
                rowColumn {
                    rowAction authorIterator.firstName, MyLibraryController.&showAuthor as MC, authorIterator.id
                    if (isSelect) {rowAction tr('default.role.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, authorIterator.id, authorIterator.toString()}
                }
                rowField authorIterator.lastName_
                if(!isSelect) {  // TODO 4.1.4: Add && isAdmin condition to display admin row fields only for admins.
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
     * number of instances, and action buttons for viewing, editing, modifying instances, and requesting books.
     *
     * **Purpose:** Generates the book listing table for either all books or books by a specific author, with actions to manage or request books, and displays options based on user admin status.
     *
     * **How it works (to implement):**
     * - Determines if the current user is an admin by calling `isAdmin()`.
     * - Creates a new instance of `MyLibraryBook`.
     * - Creates a new `UiTableSpecifier` for displaying book data.
     * - Defines the table UI with:
     *   - A header containing:
     *     - Title column.
     *     - Author column (if no specific author is provided).
     *     - Number of instances column.
     *     - "Number of Available Book Instances" label.
     *     - "Modify number of Book Instances" column.
     *     - "Request Form" column, displayed only if user is not admin.
     * - Builds a filter for `MyLibraryBook`:
     *     - Sets max number of lines to 10.
     *     - Sorts by title ascending.
     *     - Restricts to the author's books if an author is provided.
     * - Iterates through the filtered results to display:
     *   - SHOW and EDIT actions with title field.
     *   - Author field (if no specific author).
     *   - Number of instances (shown only to admins).
     *   - Number of available books field.
     *   - If user is admin, shows DELETE and ADD actions to manage book instances.
     *   - If user is not admin, shows REQUEST action to request a book.
     *
     * **Inputs:**
     * - `author`: Optional. If provided, displays only books by this author.
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the book table with appropriate columns and actions.
     */
    UiTableSpecifier buildBookTable(MyLibraryAuthor author = null) {
        // TODO 4.2.1: Determine if current user is admin by calling isAdmin() and store in isAdmin.


        MyLibraryBook book = new MyLibraryBook()
        UiTableSpecifier bookTableSpecifier = new UiTableSpecifier()
        bookTableSpecifier.ui {
            header {
                column {label book.title_}
                if (!author) {sortableFieldHeader book.author_}
                column {label "Number of instances "} // TODO 4.2.2: Add isAdmin condition to display the Number of instances only for admin.
                if (!author) {
                    label "Number of Available Book Instances"
                    column {label "Modify number of Book Instances"} // TODO 4.2.3: Add isAdmin condition to display only for admin.
                    label "Request Form" // TODO 4.2.4: Add !isAdmin condition to display the Request Form only for borrowers.
                }
            }
            TaackFilter.FilterBuilder filter =  taackFilterService.getBuilder(MyLibraryBook)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, book.title_)

            // TODO 4.2.5.1: Check if user is not admin using if(!isAdmin).
            // Inside the if block:
            // - TODO 4.2.5.2: Create a new instance of MyLibraryBookInstance named bookInstance.
            // - TODO 4.2.5.3: Add a filter to filter only available book instances
            //      by creating a new FilterExpression comparing book.listOfBookInstance_ and bookInstance.isAvailableB_
            //      using Operator.EQ, with true as the comparison value. Add this filter to the filter builder.

            if(author) {filter.addRestrictedIds(author.listOfBooks*.id as Long[])}
            iterate(
                    filter.build()) { MyLibraryBook bookIterator ->
                rowColumn {
                    rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, MyLibraryController.&showBook as MC, bookIterator.id
                    rowAction ActionIcon.EDIT * IconStyle.SCALE_DOWN, MyLibraryController.&createBook as MC, bookIterator.id
                    rowField bookIterator.title_
                }
                if (!author) {rowField bookIterator.author_}
                rowColumn {rowField bookIterator.numberOfInstances_} // TODO 4.2.6: Add !isAdmin condition to display admin columns only for admins.
                if (!author) {
                    rowField bookIterator.numberOfBooksBorrowable_
                    // TODO 4.2.7: If user is admin, add DELETE and ADD actions for managing instances.
                    // TODO 4.2.8: If user is not admin, add CREATE action to request book instance.
                    rowColumn {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&selectBookInstance as MC, bookIterator.id
                        rowAction ActionIcon.ADD * IconStyle.SCALE_DOWN, MyLibraryController.&purchaseBook as MC, bookIterator.id
                    }
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
                field numberForInstances.numberOfInstances_
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
     * Builds a table displaying all active (and optionally selectable) physical instances of a given book.
     *
     * **Purpose:** Allows librarians or users to view, select, or delete individual book instances.
     * If `isOne` is true, displays a selection action to choose a specific book instance; otherwise, displays a delete action to remove instances.
     *
     * **How it works (to implement):**
     * - Creates a new `UiTableSpecifier`.
     * - Defines the table header with:
     *   - A label for "Serial Number".
     *   - A column with a label that changes based on `isOne`:
     *     - If `isOne` is true, displays "Select Book Instance".
     *     - Otherwise, displays "Delete".
     * - Builds a filter for `MyLibraryBookInstance`:
     *   - Restricts IDs to instances belonging to the provided book.
     *   - Adds a filter for active instances.
     *   - **New feature:** Adds a filter for available instances by calling `buildIsAvailableBookInstances(book)`.
     * - Iterates over the filtered results to display:
     *   - The serial number field.
     *   - If `isOne` is true, shows a SELECT action returning the instance's serial number.
     *   - Otherwise, shows a DELETE action to remove the instance.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be displayed.
     * - `isOne`: Boolean flag indicating if the table is used for selecting a single instance (true) or managing all instances (false).
     * - `bookInstance`: Optional specific instance context (default null).
     *
     * **Outputs:** Returns a `UiTableSpecifier` configured to display and manage or select book instances.
     */
    UiTableSpecifier buildInstanceBookTable(MyLibraryBook book, isOne = false, MyLibraryBookInstance bookInstance = null) {
        UiTableSpecifier table = new UiTableSpecifier()
        table.ui {
            header {
                label "Serial Number"
                column {
                    if (isOne) {label "Select Book Instance"}
                    else {label "Delete"}
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryBookInstance).addRestrictedIds(book.listOfBookInstance*.id as Long[])
            filter.addFilter(buildIsActiveBookInstances(book))
            filter.addFilter(buildIsAvailableBookInstances(book))
            iterate(
                    filter.build()) { MyLibraryBookInstance bookInstanceIterator ->
                rowField bookInstanceIterator.serialNumber_
                rowColumn {
                    if(isOne) {
                        rowAction tr('default.serialNumber.label'), ActionIcon.SELECT * IconStyle.SCALE_DOWN, bookInstanceIterator.id, bookInstanceIterator.serialNumber.toString()
                    } else {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteBookInstances as MC, bookInstanceIterator.id, [bookId:book.id]
                    }
                }
            }
        }
    }



    /**
     * Builds a filter specifier to select only available book instances for a given book.
     *
     * **Purpose:** Allows filtering of book instances based on their availability status, ensuring that only available copies are listed for selection or borrowing.
     *
     * **How it works (to implement):**
     * - Create a new instance of `MyLibraryBookInstance`.
     * - Create a new `UiFilterSpecifier` for `MyLibraryBookInstance`.
     * - Define a UI block with:
     *   - A boolean filter field expression that checks if `isAvailableB_` is true, using `FilterExpression` and `Operator.EQ`.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be filtered.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` to filter available book instances.
     */
    UiFilterSpecifier buildIsAvailableBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isAvailableB_)
        }
    }

    /**
     * Builds a form for requesting to borrow a book.
     *
     * **Purpose:** Allows users to submit a borrow request for a specific book by selecting an available instance and specifying the request date.
     *
     * **How it works (to implement):**
     * - Retrieve the current user from `springSecurityService.currentUser` and cast to `User`.
     * - Create a new instance of `MyLibraryBorrowed` and assign the user to its `user` field.
     * - If `book` is null, initialize it using `new MyLibraryBook(params)`.
     * - Create a new `UiFormSpecifier` for the request form.
     * - Define a UI block for the form:
     *   - Add a section titled "Request Book Form".
     *   - Inside the section, add:
     *     - A hidden field for `borrowed.user_` to pass the user in the form without display.
     *     - A field for `borrowed.requestDate_`.
     *     - An ajaxField for `borrowed.bookInstance_`, using `MyLibraryController.selectBookInstanceOne` as the controller action and passing the book ID.
     *   - Define the form action linked to `MyLibraryController.saveBookForm`.
     *
     * **Inputs:**
     * - `book`: The book being requested.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the book request form.
     */
    UiFormSpecifier buildRequestBookForm(MyLibraryBook book) {
        User user = springSecurityService.currentUser as User
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        borrowed.user = user
        book ?= new MyLibraryBook(params)
        UiFormSpecifier requestBookFormSpecifier = new UiFormSpecifier()

        requestBookFormSpecifier.ui borrowed, {
            section "Request Book Form", {
                hiddenField borrowed.user_
                field borrowed.requestDate_
                ajaxField borrowed.bookInstance_, MyLibraryController.&selectBookInstanceOne as MC, book.id
            }
            formAction MyLibraryController.&saveBookForm as MC
        }
    }


    /*------------------------------------------------------------*/
    /* History & Borrowing Menu                                   */
    /*------------------------------------------------------------*/

    /**
     * Builds a table displaying user borrow records, including book details, request and approval dates,
     * status, and actions to approve or return books depending on their current status, with logic adapting based on user role.
     *
     * **Purpose:** Allows librarians to view and manage borrow records for all users, and users to view their own borrowed books.
     *
     * **How it works (to implement):**
     * - Determines if the current user is an admin by calling `isAdmin()`.
     * - Accepts additional parameters:
     *   - `showUser`: If provided, shows borrow records for that specific user.
     *   - `isUser`: Indicates if the viewer is a user (true) or admin (false).
     * - Creates instances of:
     *   - `MyLibraryBook` to reference book fields.
     *   - `MyLibraryBorrowed` to access borrow record fields.
     *   - `MyLibraryBookInstance` to link borrow records to physical copies.
     * - Creates a new `UiTableSpecifier` for displaying borrow data.
     * - Defines a UI block with:
     *   - Headers for book title, author, status (if currently borrowed), request/approval dates, return/approve actions, and user column if admin.
     * - Retrieves the current user from `springSecurityService` or uses `showUser` if provided.
     * - Builds a filter:
     *   - Filters records for the current user if not admin, or for `showUser` if provided.
     *   - Filters by returnDate nullity to distinguish current vs. past borrowings.
     * - Iterates through filtered results to display:
     *   - SHOW action for each borrow record with book title.
     *   - Book author.
     *   - Status of approval (if currently borrowed).
     *   - Request and approval dates.
     *   - Return action if currently borrowed and approved (for users).
     *   - User username (for admins).
     *   - Approve action (for admins if no specific user is shown).
     *
     * **Inputs:**
     * - `isCurrently`: Boolean flag indicating if table shows current borrowings (true) or past borrowings (false).
     * - `showUser`: Optional. Displays borrow records for a specific user if provided.
     * - `isUser`: Boolean flag indicating if viewer is a user (true) or admin (false).
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the borrow records table with appropriate columns and actions.
     */
    UiTableSpecifier buildUserBorrowsTable(isCurrently = false, User showUser = null, isUser = false) {
        // TODO 4.3.1: Determine if current user is admin by calling isAdmin() and store in isAdmin.

        MyLibraryBook book = new MyLibraryBook()
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        UiTableSpecifier buildUserBorrowsSpecifier = new UiTableSpecifier()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()

        buildUserBorrowsSpecifier.ui {
            header {
                sortableFieldHeader borrowed.bookInstance_,bookInstance.book_,book.title_
                sortableFieldHeader borrowed.bookInstance_,bookInstance.book_,book.author_
                if (isCurrently) {label borrowed.statusOfApproval_}
                label borrowed.requestDate_
                label borrowed.approvalDate_
                if (isCurrently) column {label "Return Book"} // TODO 4.3.2: Add && !isAdmin condition to display "Return Book" column only for users in borrowed menu
                // TODO 4.3.3: Add condition isAdmin, to display borrowed user and Approve Book (if !showUser) columns
                column { label borrowed.user_ }
                label "Approve Book"
                }

            // TODO 4.3.4: Retrieve currentUser from springSecurityService or use showUser if provided.

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryBorrowed)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, borrowed.bookInstance_,bookInstance.book_,book.title_)

            // TODO 4.3.5: If viewer is not user or showUser is provided, filter by currentUser.

            if(isCurrently) {filter.addFilter(new FilterExpression(null, Operator.EQ, borrowed.returnDate_))}
            else {filter.addFilter(new FilterExpression(null, Operator.NE, borrowed.returnDate_))}

            iterate(
                    filter.build()) { MyLibraryBorrowed borrowedIterator ->
                rowColumn {
                    rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, MyLibraryController.&showBorrowed as MC, borrowedIterator.id
                    rowField borrowedIterator.bookInstance.book.title
                }
                rowField borrowedIterator.bookInstance.book.author.toString()
                if(isCurrently) {rowField borrowedIterator.statusOfApproval_}
                rowField borrowedIterator.requestDate_
                rowField borrowedIterator.approvalDate_
                if (isCurrently) {  // TODO 4.3.6: Add && !isAdmin condition to display rowColumn only for borrowers.
                    rowColumn {
                        if (borrowedIterator.statusOfApproval == ApprovalStatus.APPROVED) {
                            rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&returnBook as MC, borrowedIterator.id
                        }
                    }
                }
                // TODO 4.3.7: Add condition isAdmin, to display borrowed user and Approve Book (if !showUser) rowColumns
                rowColumn { rowField borrowedIterator.user.username_ }
                rowColumn {rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&approveBook as MC, borrowedIterator.id}
            }
        }
    }

    /**
     * Builds a filter specifier for filtering user borrow records by book title.
     *
     * **Purpose:** Allows users or librarians to filter borrow records based on the title of the borrowed book.
     *
     * **How it works (to implement):**
     * - Create instances of:
     *   - `MyLibraryBook` to reference book fields.
     *   - `MyLibraryBorrowed` to access borrow record fields.
     *   - `MyLibraryBookInstance` to link borrow records to physical copies.
     * - Create a new `UiFilterSpecifier` for `MyLibraryBorrowed`.
     * - Define a UI block with:
     *   - A section titled "Borrows Filter".
     *   - Inside the section, add a filter field for the book title.
     *
     * **Inputs:** None directly; uses model instances to reference fields.
     *
     * **Outputs:** Returns a `UiFilterSpecifier` configured to filter borrow records by book title.
     */
    UiFilterSpecifier buildUserBorrowsFilter(User user = null) {
        MyLibraryBook book = new MyLibraryBook()
        UiFilterSpecifier UserBorrowsFilterSpecifier = new UiFilterSpecifier()
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()

        UserBorrowsFilterSpecifier.ui MyLibraryBorrowed, {
            if (user) hiddenId(user.id)
            section "Borrows Filter", {
                filterField borrowed.bookInstance_,bookInstance.book_,book.title_
            }
        }
    }


    /**
     * Builds a form for approving a book borrow request.
     *
     * **Purpose:** Allows librarians to approve a borrow request by setting its approval date and status.
     *
     * **How it works (to implement):**
     * - Creates a new `UiFormSpecifier` for the approval form.
     * - Defines a UI block for the form:
     *   - Adds a section titled "Approve Book Form".
     *   - Inside the section, adds fields for:
     *     - `approvalDate_` to set the date of approval.
     *     - `statusOfApproval_` to set the approval status.
     *   - Defines formAction linking to `MyLibraryController.saveApprovalBookForm` to handle form submission and saving.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record being approved.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the approval form for the borrow request.
     */
    UiFormSpecifier buildApproveBookTable(MyLibraryBorrowed borrowed) {
        UiFormSpecifier approveBookSpecifier = new UiFormSpecifier()

        approveBookSpecifier.ui borrowed, {
            section "Approve Book Form", {
                field borrowed.approvalDate_
                field borrowed.statusOfApproval_
            }
            formAction MyLibraryController.&saveApprovalBookForm as MC
        }
    }




    /**
     * Builds a form for recording the return of a borrowed book.
     *
     * **Purpose:** Allows librarians or users to input the return date when a borrowed book is returned.
     *
     * **How it works (to implement):**
     * - Creates a new `UiFormSpecifier` for the return book form.
     * - Defines a UI block for the form:
     *   - Adds a section titled "Request Return Book Form".
     *   - Inside the section, adds a field for:
     *     - `returnDate_` to specify the date the book was returned.
     *   - Defines formAction linking to `MyLibraryController.saveReturnBookForm` to handle the submission and saving of the return date.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record for which the return is being recorded.
     *
     * **Outputs:** Returns a `UiFormSpecifier` representing the return book form.
     */
    UiFormSpecifier buildRequestReturnBookForm(MyLibraryBorrowed borrowed) {
        UiFormSpecifier requestBookFormSpecifier = new UiFormSpecifier()

        requestBookFormSpecifier.ui borrowed, {
            section "Request Return Book Form", {
                field borrowed.returnDate_
            }
            formAction MyLibraryController.&saveReturnBookForm as MC
        }
    }

    /**
     * Builds a read-only detail view specifier for displaying information about a borrow record.
     *
     * **Purpose:** Generates a UI specifier to show all relevant details of a borrowed book record in a non-editable format.
     *
     * **How it works (to implement):**
     * - Creates a new `UiShowSpecifier` for the borrow record.
     * - Defines a UI block displaying the following fields using `fieldLabeled`:
     *   - Book title.
     *   - Book author.
     *   - Username of the user who borrowed the book.
     *   - Status of approval.
     *   - Request date.
     *   - Approval date.
     *   - Return date.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record whose details are to be shown.
     *
     * **Outputs:** Returns a `UiShowSpecifier` configured to display the borrow record's details.
     */
    UiShowSpecifier buildBorrowedShow(MyLibraryBorrowed borrowed) {
        UiShowSpecifier borrowedShowSpecifier = new UiShowSpecifier()

        borrowedShowSpecifier.ui(borrowed, {
            fieldLabeled borrowed.bookInstance.book.title_
            fieldLabeled borrowed.bookInstance.book.author_
            fieldLabeled borrowed.user.username_
            fieldLabeled borrowed.statusOfApproval_
            fieldLabeled borrowed.requestDate_
            fieldLabeled borrowed.approvalDate_
            fieldLabeled borrowed.returnDate_
        })
    }


    /*------------------------------------------------------------*/
    /* Users Menu                                                 */
    /*------------------------------------------------------------*/

    /**
     * Builds a table displaying all users with their username and authorities.
     *
     * **Purpose:** Allows admins to view the list of registered users in the system along with their roles.
     *
     * **How it works (to implement):**
     * - Creates instances of:
     *   - `MyLibraryBorrowed` (unused here but declared, likely for consistency in UI service methods).
     *   - `User` to access user fields.
     * - Creates a new `UiTableSpecifier` to define the table structure.
     * - Defines the table UI:
     *   - A header with columns for username and authorities.
     * - Builds a filter using `taackFilterService.getBuilder(User)`:
     *   - Sorts users by username ascending.
     *   - Limits results to 10 users per page.
     * - Iterates over the filtered results to display:
     *   - A SHOW action for each user linking to the `showUser` controller action.
     *   - Username field.
     *   - Authorities field.
     *
     * **Inputs:** None directly; retrieves all users from the database.
     *
     * **Outputs:** Returns a `UiTableSpecifier` rendering the users table with their username and authorities.
     */
    UiTableSpecifier buildUsersTable() {
        // TODO 5.1.1: Create a new instance of MyLibraryBorrowed.
        // TODO 5.1.2: Create a new UiTableSpecifier named buildUsersSpecifier.
        // TODO 5.1.3: Create a new instance of User.

        // TODO 5.1.4: Define buildUsersSpecifier.ui block.
        // Inside ui block:
        // - TODO 5.1.5: Define header block with labels for username and authorities.

        // - TODO 5.1.6: Build a TaackFilter for User, sorted by username ascending, max 10 per page.

        // - TODO 5.1.7: Iterate over taackFilter results.
        // For each user:
        //   - TODO 5.1.8: Add a rowColumn with SHOW action linking to MyLibraryController.showUser and rowField for username.
        //   - TODO 5.1.9: Add rowField for user authorities.

        //delete after implementation
        return new UiTableSpecifier()
    }

    /**
     * Builds a read-only detail view specifier for displaying user information.
     *
     * **Purpose:** Shows the username, first name, last name, and authorities of a user in a non-editable format.
     *
     * **Inputs:**
     * - `user`: The user whose details are to be displayed.
     *
     * **Outputs:** Returns a `UiShowSpecifier` configured to display the user's details.
     */
    UiShowSpecifier buildUserShow(User user) {
        // TODO 5.3.1: Create a new UiShowSpecifier named userShowSpecifier.
        // TODO 5.3.2: Define userShowSpecifier.ui block for the given user.
        // Inside ui block:
        // - TODO 5.3.3: Add fieldLabeled for user.username_.
        // - TODO 5.3.4: Add fieldLabeled for user.firstName_.
        // - TODO 5.3.5: Add fieldLabeled for user.lastName_.
        // - TODO 5.3.6: Add fieldLabeled for user.authorities_.

        //delete after implementation
        return new UiShowSpecifier()
    }




}



