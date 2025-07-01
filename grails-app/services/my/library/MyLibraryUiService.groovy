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
                // TODO 2.2.1: Add a filterFieldExpressionBool as shown in the example above.
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
        // TODO 2.2.2.1: Create a new instance of MyLibraryAuthor.
        UiFilterSpecifier authorFilterSpecifier = new UiFilterSpecifier()

        authorFilterSpecifier.ui MyLibraryAuthor, {
            section "Author Filter", {
                // TODO 2.2.2.2: Add a filterField for author's lastName_.
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
                column {
                    // TODO 2.1.1: Add label for author's first name using author.firstName_
                }
                // TODO 2.1.2: Add label for author's last name using author.lastName_
                if(!isSelect) {
                    // TODO 2.1.3: Add label for isActive status and "Delete Author" action column if not in select mode
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryAuthor)
            // TODO 2.1.4: Add the following requirements to the filter:
            // - max 10 lines per page using setMaxNumberOfLine()
            // - ascending order by last name using setSortOrder(TaackFilter.Order order, FieldInfo field)
            // - add active filter if isSelect is true, implemented later on: buildIsActiveAuthorFilter(author)

            iterate(
                    filter.build()) { MyLibraryAuthor authorIterator ->
                rowColumn {
                    // TODO 2.10: Add rowAction to showAuthor with author's first name as label
                    if (isSelect) {
                        // TODO 3: Add SELECT action icon to select author with their id and string representation
                    }
                }
                // TODO 2.1.5: Display author's last name as rowField
                if(!isSelect) {
                    // TODO 2.1.6: Display isActive status
                    rowColumn {
                        // TODO 2.7.1: Add DELETE action icon linked to deleteAuthor controller action
                        // TODO 2.7.2: Add ACTIVATE action icon linked to activateAuthor controller action
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
        // TODO 2.4.1: If author is null, initialize it with new MyLibraryAuthor(params).
        UiFormSpecifier createAuthorSpecifier = new UiFormSpecifier()
        createAuthorSpecifier.ui author, {
            section "Author details", {
                // TODO 2.4.2: Add fields for firstName, lastName, dateOfBirth, and isActive as shown in the example.
            }
            // TODO 2.4.3: Define formAction linking to MyLibraryController.saveAuthor as MC.
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
            // TODO 2.11: Add fieldLabeled lines to display firstName, lastName, dateOfBirth, and isActive fields.
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
        // TODO 3.1.1: Create a new instance of MyLibraryBook named book.
        // TODO 3.1.2: Create a new UiTableSpecifier named bookTableSpecifier.
        // TODO 3.1.3: Define bookTableSpecifier.ui block.
        // Inside ui block:
        // - TODO 3.1.4: Define header block.
        //   - TODO 3.1.5: Add column for book title with label.
        //   - TODO 3.1.6: If author is null, add sortableFieldHeader for book author.
        //   - TODO 3.1.7: Add column for "Number of instances".
        //   - TODO 3.1.8: If author is null, add column for "Modify number of Book Instances".
        // - TODO 3.1.9: Build filter for MyLibraryBook using taackFilterService.getBuilder.
        //   - Set max number of lines to 10.
        //   - Set sort order by title ascending.
        //   - If author is provided, restrict filter to books by that author using addRestrictedIds(author.listOfBooks*.id as Long[])
        // - TODO 3.1.10: Iterate over filter results using iterate(filter.build()).
        //   Inside iterate block:
        //   - TODO 3.1.11: Display title field.
        //   - TODO 3.1.12: Add EDIT (createBook) action with title field in a rowColumn, this will be used to modify a book.
        //   - TODO 3.13: Add SHOW (showBook) action above title field in the rowColumn.
        //   - TODO 3.1.13: If author is null, display author field.
        //   - TODO 3.8.1: Display number of instances (call getNumberOfInstances) and show as rowField.
        //   - TODO 3.8.2: If author is null, add DELETE and ADD actions for managing book instances.

        //delete the following statement when method implemented
        return new UiTableSpecifier()
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
        // TODO 3.4.1: If book is null, initialize it with new MyLibraryBook(params).
        // TODO 3.4.2: Create a new UiFormSpecifier named bookFormSpecifier.
        // TODO 3.4.3: Define bookFormSpecifier.ui block.
        // Inside ui block:
        // - TODO 3.4.4: Define a section titled "Book details".
        //   - TODO 3.4.5: Add field for book.title_.
        //   - TODO 3.4.6: Add ajaxField for book.author_ linked to MyLibraryController.&selectAuthor.
        //       Example:  ajaxField FieldInfo, MyLibraryController.&methode as MC
        //   - TODO 3.4.7: Add field for book.numberOfPages_.
        //   - TODO 3.4.8: Add field for book.description_.
        // - TODO 3.4.9: Define formAction linking to MyLibraryController.saveBook as MC.

        //delete the following statement when method implemented
        return new UiFormSpecifier()
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
        // TODO 3.2.1: Create a new instance of MyLibraryBook named book.
        // TODO 3.2.2: Create a new UiFilterSpecifier named bookFilterSpecifier.
        // TODO 3.2.3: Define bookFilterSpecifier.ui block.
        // Inside ui block:
        // - TODO 3.2.4: Define a section titled "Book Filter".
        //   - TODO 3.2.5: Add filterField for book.title_.

        //delete the following statement when method implemented
        return new UiFilterSpecifier()
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
        // TODO 3.9.1: Create a new instance of NumberForInstances named numberForInstances.
        // TODO 3.9.2: If book is null, initialize it with new MyLibraryBook(params).
        // TODO 3.9.3: Create a new UiFormSpecifier named bookPurchaseSpecifier.
        // TODO 3.9.4: Define bookPurchaseSpecifier.ui block.
        // Inside ui block:
        // - TODO 3.9.5: Define a section titled "Purchase Number".
        //   - TODO 3.9.6: Add field for numberForInstances.numberOfInstances_.
        // - TODO 3.9.7: Define formAction linking to MyLibraryController.purchaseAndSaveBook as MC.

        //delete the following statement when method implemented
        return new UiFormSpecifier()
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
        // TODO 3.14.1: Create a new UiShowSpecifier named bookShowSpecifier.
        // TODO 3.14.2: Define bookShowSpecifier.ui block for the given book.
        // Inside ui block:
        // - TODO 3.14.3: Add fieldLabeled for book.title_.
        // - TODO 3.14.4: Add fieldLabeled for book.author_.
        // - TODO 3.14.5: Add fieldLabeled for book.numberOfPages_.
        // - TODO 3.14.6: Add fieldLabeled for book.description_.
        // - TODO 3.14.7: Add fieldLabeled for book.numberOfInstances_.

        //delete the following statement when method implemented
        return new UiShowSpecifier()
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
        // TODO 3.16.1: Create a new instance of MyLibraryBookInstance named bookInstance.
        // TODO 3.16.2: Create a new UiFilterSpecifier named bookInstanceFilterSpecifier.
        // TODO 3.16.3: Define bookInstanceFilterSpecifier.sec block for MyLibraryBookInstance.
        // Inside sec block:
        // - TODO 3.16.4: Add filterFieldExpressionBool with FilterExpression comparing bookInstance.isActive_ to true using Operator.EQ.

        //delete the following statement when method implemented
        return new UiFilterSpecifier()
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
    UiTableSpecifier buildInstanceBookTable(MyLibraryBook book, MyLibraryBookInstance bookInstance = null) {
        // TODO 3.17.1: Create a new UiTableSpecifier named table.
        // TODO 3.17.2: Define table.ui block.
        // Inside ui block:
        // - TODO 3.17.3: Define header block.
        //   - TODO 3.17.4: Add label for "Serial Number".
        //   - TODO 3.17.5: Add column with label "Delete".
        // - TODO 3.17.6: Build a TaackFilter.FilterBuilder for MyLibraryBookInstance.
        //   - Restrict IDs to book.listOfBookInstance IDs.
        //   - Add filter by calling buildIsActiveBookInstances(book).
        // - TODO 3.17.7: Iterate over filter.build().
        // Inside iterate block:
        //   - TODO 3.17.8: Display rowField for bookInstanceIterator.serialNumber_.
        //   - TODO 3.17.9: Add rowAction for DELETE linked to MyLibraryController.deleteBookInstances, passing instance ID and bookId.

        //delete the following statement when method implemented
        return new UiTableSpecifier()
    }
}

