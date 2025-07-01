package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.web.api.WebAttributes
import org.codehaus.groovy.runtime.MethodClosure
import taack.render.TaackSaveService
import taack.render.TaackUiService
import taack.ui.dsl.*
import taack.ui.dsl.common.ActionIcon

/**
 * Controller responsible for all UI interactions related to authors and books
 * managed by *MyLibrary*. Almost every action simply delegates the UI building
 * to {@link MyLibraryUiService} and lets *Taack UI* handle the rendering.
 *
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Display tables and filters for authors and books</li>
 *   <li>Open modal forms for creating/editing entities</li>
 *   <li>Persist changes through {@link TaackSaveService}</li>
 *   <li>Soft‑delete (deactivate) and reactivate entities</li>
 *   <li>Create physical book instances in bulk</li>
 * </ul>
 *
 * All actions are written in Groovy/Grails style ("def" instead of explicit
 * return types) and many are annotated with {@code @Transactional} to make sure
 * database changes are committed or rolled back atomically.
 */
@Secured(['ROLE_ADMIN'])
@GrailsCompileStatic
class MyLibraryController implements WebAttributes {
    TaackUiService taackUiService
    MyLibraryUiService myLibraryUiService

    /*------------------------------------------------------------*/
    /* General actions                                            */
    /*------------------------------------------------------------*/

    /**
     * Landing action of the controller. Immediately redirects to {@link #listAuthor()} ‑
     * our default screen that lists all authors.
     */
    def index() {
        redirect action: 'listAuthor'
    }

    /*------------------------------------------------------------*/
    /* Author menu                                                */
    /*------------------------------------------------------------*/

    /**
     * Displays the table of authors along with a filter bar and a *Create Author* button.
     *
     * **Purpose:** Renders the main author listing screen where users can:
     * - View the list of authors.
     * - Filter authors using the filter bar.
     * - Access the form to create a new author via the Create button.
     *
     * **How it works (to implement):**
     * - Call `myLibraryUiService.buildAuthorTable()` to build the table specifier for displaying authors.
     * - Call `myLibraryUiService.buildAuthorFilter()` to build the filter specifier.
     * - Use `taackUiService.show` to render a UI block containing:
     *   - The filter and table combined using `tableFilter`.
     *   - A *Create* menu icon that opens the `createAuthor` modal when clicked.
     * - Add the general menu by calling `myLibraryUiService.buildMenu()`.
     *
     * **Inputs:** None directly; relies on UI services to build table and filter specifiers.
     * **Outputs:** Renders the author list screen for the user.
     *
     * Adds a Create icon button that opens the createAuthor action.
     */
    def listAuthor() {
        UiTableSpecifier tableAuthorSpecifier = myLibraryUiService.buildAuthorTable()
        UiFilterSpecifier filterAuthorSpecifier = myLibraryUiService.buildAuthorFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            // TODO 2.3: Use tableFilter to combine filterAuthorSpecifier and tableAuthorSpecifier, and add a Create menu icon as shown in the example.
            // Example : tableFilter filterSpecifier, tableSpecifier, {
            //                menuIcon ActionIcon.CREATE, this.&methode as MethodClosure
            //           }
        }, myLibraryUiService.buildMenu())
    }

    /**
     * Opens a modal form for creating or editing an author.
     *
     * **Purpose:** Displays the author form inside a modal window for user input or editing.
     *
     * **How it works (to implement):**
     * - Build the form specifier by calling `myLibraryUiService.buildAuthorForm(author)`.
     * - Use `taackUiService.show` to render a modal containing the form.
     *
     * **Inputs:**
     * - `author`: The author object to edit, or null for creating a new author.
     *
     * **Outputs:** Displays the modal form to the user.
     */
    def createAuthor(MyLibraryAuthor author) {
        // TODO 2.5.1: Build formAuthorSpecifier by calling myLibraryUiService.buildAuthorForm(author).

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                // TODO 2.5.2: Add the formAuthorSpecifier to the modal using the form keyword.
            }
        })
    }

    /**
     * Soft-deletes the given author by marking them as inactive.
     *
     * **Purpose:** Deactivates an author without removing their record from the database, allowing for future reactivation if needed.
     *
     * **How it works (to implement):**
     * - Sets the `isActive` field of the provided `author` to `false`.
     * - Redirects the user back to the author listing screen after deactivation.
     *
     * **Inputs:**
     * - `author`: The author to deactivate.
     *
     * **Outputs:** Updates the author's active status in the database and redirects to the author list view.
     */
    @Transactional
    def deleteAuthor(MyLibraryAuthor author) {
        // TODO 2.8.1: Set author.isActive to false to deactivate the author.
        // TODO 2.8.2: Redirect to the 'listAuthor' action to refresh the author list view.
    }

    /**
     * Reactivates a previously deactivated author.
     *
     * **Purpose:** Marks an inactive author as active again, restoring their visibility and usability in the system.
     *
     * **How it works (to implement):**
     * - Sets the `isActive` field of the provided `author` to `true`.
     * - Redirects the user back to the author listing screen after reactivation.
     *
     * **Inputs:**
     * - `author`: The author to reactivate.
     *
     * **Outputs:** Updates the author's active status in the database and redirects to the author list view.
     */
    @Transactional
    def activateAuthor(MyLibraryAuthor author) {
        // TODO 2.9.1: Set author.isActive to true to reactivate the author.
        // TODO 2.9.2: Redirect to the 'listAuthor' action to refresh the author list view.
    }

    /**
     * Saves a new or edited author and reloads the page, or re-renders the form with validation errors if saving fails.
     *
     * **Purpose:** Persists author data changes in the database and ensures UI feedback.
     *
     * **How it works (to implement):**
     * - Uses `taackSaveService.saveThenReloadOrRenderErrors` with `MyLibraryAuthor` as the argument to:
     *   - Save the author object.
     *   - Reload the page if saving succeeds.
     *   - Re-render the form showing validation errors if saving fails.
     *
     * **Inputs:** None directly; uses request parameters bound to MyLibraryAuthor.
     * **Outputs:** Persists data and updates the UI accordingly.
     */
    @Transactional
    def saveAuthor() {
        // TODO 2.6: Call taackSaveService.saveThenReloadOrRenderErrors with MyLibraryAuthor to handle saving and reloading logic.
    }

    /**
     * Displays a modal with the author's details and a list of their books.
     *
     * **Purpose:** Allows users to view an author's information along with the books they have written, all within a single modal window.
     *
     * **How it works (to implement):**
     * - Build the table specifier for books written by this author by calling `myLibraryUiService.buildBookTable(author)`.
     * - Build the filter specifier for books by calling `myLibraryUiService.buildBookFilter()`.
     * - Build the show specifier for the author's details by calling `myLibraryUiService.buildAuthorShow(author)`.
     * - Use `taackUiService.show` to render a UI block containing:
     *   - A modal that displays:
     *     - The author's details using `show showBookSpecifier`.
     *     - A tableFilter block combining the book filter and table specifiers.
     *
     * **Inputs:**
     * - `author`: The author whose details and books are to be displayed.
     *
     * **Outputs:** Renders a modal showing the author's details and their list of books.
     */
    def showAuthor(MyLibraryAuthor author) {
        // TODO 2.12.1: Build tableBookSpecifier by calling myLibraryUiService.buildBookTable(author).
        // TODO 2.12.2: Build filterBookSpecifier by calling myLibraryUiService.buildBookFilter().
        // TODO 2.12.3: Build showBookSpecifier by calling myLibraryUiService.buildAuthorShow(author).

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                // TODO 2.12.4: Show author details using showBookSpecifier.
                // TODO 2.12.5: Add tableFilter combining filterBookSpecifier and tableBookSpecifier.
            }
        })
    }

    /**
     * Opens a modal selector listing all authors for selection.
     *
     * **Purpose:** Allows users to select an author from a list, typically used in forms requiring author association.
     *
     * **How it works (to implement):**
     * - Build the author table specifier in selection mode by calling `myLibraryUiService.buildAuthorTable(true)`.
     * - Build the author filter specifier by calling `myLibraryUiService.buildAuthorFilter()`.
     * - Use `taackUiService.show` to render a modal containing:
     *   - A tableFilter combining the filter and table specifiers.
     *
     * **Inputs:** None directly.
     *
     * **Outputs:** Displays a modal for author selection.
     */
    def selectAuthor() {
        // TODO 3.5.1: Build tableAuthorSpecifier by calling myLibraryUiService.buildAuthorTable(true).
        // TODO 3.5.2: Build filterAuthorSpecifier by calling myLibraryUiService.buildAuthorFilter().
        // TODO 3.5.3: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.5.4: Define a modal block.
        //   - TODO 3.5.5: Add tableFilter combining filterAuthorSpecifier and tableAuthorSpecifier.
    }

    /*------------------------------------------------------------*/
    /* Book menu                                                  */
    /*------------------------------------------------------------*/

    /**
     * Displays the table of books along with a filter bar and a *Create Book* button.
     *
     * **Purpose:** Renders the main book listing screen where users can:
     * - View the list of books.
     * - Filter books using the filter bar.
     * - Access the form to create a new book via the Create button.
     *
     * **How it works (to implement):**
     * - Build the table specifier by calling `myLibraryUiService.buildBookTable()`.
     * - Build the filter specifier by calling `myLibraryUiService.buildBookFilter()`.
     * - Use `taackUiService.show` to render the UI block containing:
     *   - A tableFilter combining the filter and table specifiers.
     *   - A menu icon for creating a new book.
     * - Include the general menu built by `myLibraryUiService.buildMenu()`.
     *
     * **Inputs:** None directly; uses UI services to build specifiers.
     *
     * **Outputs:** Renders the book list screen with filtering and creation options.
     */
    def listBook() {
        // TODO 3.3.1: Build tableBookSpecifier by calling myLibraryUiService.buildBookTable().
        // TODO 3.3.2: Build filterBookSpecifier by calling myLibraryUiService.buildBookFilter().
        // TODO 3.3.3: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.3.4: Add tableFilter combining filterBookSpecifier and tableBookSpecifier.
        // - TODO 3.3.5: Add menuIcon for CREATE action linked to createBook.
        // TODO 3.3.6: Include the general menu by calling myLibraryUiService.buildMenu().
    }

    /**
     * Opens a modal form for creating or editing a book.
     *
     * **Purpose:** Displays the book form inside a modal window for user input or editing.
     *
     * **How it works (to implement):**
     * - Build the form specifier by calling `myLibraryUiService.buildBookForm(book)`.
     * - Use `taackUiService.show` to render a modal containing the form.
     *
     * **Inputs:**
     * - `book`: The book object to edit, or null for creating a new book.
     *
     * **Outputs:** Displays the modal form to the user.
     */
    def createBook(MyLibraryBook book) {
        // TODO 3.6.1: Build tableFormSpecifier by calling myLibraryUiService.buildBookForm(book).
        // TODO 3.6.2: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.6.3: Define a modal block.
        //   - TODO 3.6.4: Add form using tableFormSpecifier.
    }

    /**
     * Opens a modal form to specify the number of book instances to purchase for a given book.
     *
     * **Purpose:** Allows librarians to input how many physical copies of a book to add to the library inventory.
     *
     * **How it works (to implement):**
     * - Build the purchase form specifier by calling `myLibraryUiService.buildBookPurchase(book)`.
     * - Use `taackUiService.show` to render a modal containing the form.
     *
     * **Inputs:**
     * - `book`: The book for which instances are being purchased.
     *
     * **Outputs:** Displays the modal form to the user.
     */
    def purchaseBook(MyLibraryBook book) {
        // TODO 3.10.1: Build tableAddBookInstanceSpecifier by calling myLibraryUiService.buildBookPurchase(book).
        // TODO 3.10.2: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.10.3: Define a modal block.
        //   - TODO 3.10.4: Add form using tableAddBookInstanceSpecifier.
    }

    /**
     * Creates the specified number of book instances for a given book and reloads the UI block via AJAX.
     *
     * **Purpose:** Adds multiple physical copies of a book to the library inventory in one action.
     *
     * **How it works (to implement):**
     * - Loop from 0 to the number specified in `numberForInstances.numberOfInstances`.
     * - In each iteration:
     *   - Create a new instance of `MyLibraryBookInstance`.
     *   - Assign the current book to the new instance.
     *   - Add the new book instance to the book's list of instances.
     * - Reload the UI block using `taackUiService.ajaxReload()` to reflect the changes immediately.
     *
     * **Inputs:**
     * - `numberForInstances`: Contains the number of copies to create.
     * - `book`: The logical book entity to which copies belong.
     *
     * **Outputs:** Adds the specified book instances and refreshes the UI.
     */
    @Transactional
    def purchaseAndSaveBook(NumberForInstances numberForInstances, MyLibraryBook book) {
        // TODO 3.11.1: Loop from i = 0 to numberForInstances.numberOfInstances.
        // Inside loop:
        // - TODO 3.11.2: Create a new MyLibraryBookInstance named newBookInstance.
        // - TODO 3.11.3: Set newBookInstance.book to book.
        // - TODO 3.11.4: Add newBookInstance to book.listOfBookInstance.
        // TODO 3.11.5: After loop, call taackUiService.ajaxReload() to refresh the UI.
    }

    /**
     * Saves a new or edited book and reloads the page, or re-renders the form with validation errors if saving fails.
     *
     * **Purpose:** Persists book data changes in the database and ensures UI feedback.
     *
     * **How it works (to implement):**
     * - Uses `taackSaveService.saveThenReloadOrRenderErrors` with `MyLibraryBook` as the argument to:
     *   - Save the book object.
     *   - Reload the page if saving succeeds.
     *   - Re-render the form showing validation errors if saving fails.
     *
     * **Inputs:** None directly; uses request parameters bound to MyLibraryBook.
     *
     * **Outputs:** Persists data and updates the UI accordingly.
     */
    @Transactional
    def saveBook() {
        // TODO 3.7: Call taackSaveService.saveThenReloadOrRenderErrors with MyLibraryBook to handle saving and reloading logic.
    }

    /**
     * Displays a modal with the book's details in read-only format.
     *
     * **Purpose:** Allows users to view the details of a book in a non-editable modal window.
     *
     * **How it works (to implement):**
     * - Build the show specifier for the book by calling `myLibraryUiService.buildBookShow(book)`.
     * - Use `taackUiService.show` to render a modal containing the show specifier.
     *
     * **Inputs:**
     * - `book`: The book whose details are to be displayed.
     *
     * **Outputs:** Displays a modal showing the book's details.
     */
    def showBook(MyLibraryBook book) {
        // TODO 3.15.1: Build showBookSpecifier by calling myLibraryUiService.buildBookShow(book).
        // TODO 3.15.2: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.15.3: Define a modal block.
        //   - TODO 3.15.4: Add show using showBookSpecifier.
    }

    /**
     * Opens a modal that shows all physical instances of a given book.
     *
     * **Purpose:** Allows librarians to view and select specific book instances for actions such as lending or deletion.
     *
     * **How it works (to implement):**
     * - Build the book instance table specifier by calling `myLibraryUiService.buildInstanceBookTable(book)`.
     * - Use `taackUiService.show` to render a modal containing the table of book instances.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be displayed.
     *
     * **Outputs:** Displays a modal listing all instances of the book.
     */
    def selectBookInstance(MyLibraryBook book) {
        // TODO 3.12.1: Build bookInstanceTableSpecifier by calling myLibraryUiService.buildInstanceBookTable(book).
        // TODO 3.12.2: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.12.3: Define a modal block (with true flag).
        //   - TODO 3.12.4: Add table using bookInstanceTableSpecifier.
    }

    /**
     * Soft-deletes (deactivates) a single physical book instance and refreshes the UI tables to reflect the change.
     *
     * **Purpose:** Allows librarians to deactivate specific book copies and update the view immediately.
     *
     * **How it works (to implement):**
     * - Retrieve the parent book using `params.bookId`.
     * - Build the table specifier for book instances by calling `myLibraryUiService.buildInstanceBookTable(book)`.
     * - Set `bookInstance.isActive` to false to deactivate it.
     * - Save the updated book instance with flush and without validation.
     * - Use `taackUiService.show` to render:
     *   - A closeModalAndUpdateBlock block containing:
     *     - A tableFilter combining the book filter and book table.
     *     - A modal containing the updated book instance table.
     *
     * **Inputs:**
     * - `bookInstance`: The specific physical copy to deactivate.
     *
     * **Outputs:** Deactivates the book instance and refreshes the UI to reflect the change.
     */
    @Transactional
    def deleteBookInstances(MyLibraryBookInstance bookInstance) {
        // TODO 3.18.1: Retrieve the book by calling MyLibraryBook.get with params.bookId.
        // TODO 3.18.2: Build bookInstanceTable by calling myLibraryUiService.buildInstanceBookTable(book).
        // TODO 3.18.3: Set bookInstance.isActive to false.
        // TODO 3.18.4: Save bookInstance with flush true and validate false.
        // TODO 3.18.5: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 3.18.6: Define closeModalAndUpdateBlock.
        //   Inside closeModalAndUpdateBlock:
        //   - TODO 3.18.7: Add tableFilter combining myLibraryUiService.buildBookFilter() and buildBookTable().
        //   - TODO 3.18.8: Add menuIcon for CREATE action linked to createBook.
        //   - TODO 3.18.9: Add modal block containing table bookInstanceTable.
    }

}