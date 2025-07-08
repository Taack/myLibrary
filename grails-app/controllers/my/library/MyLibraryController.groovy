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
@Secured(['ROLE_ADMIN','ROLE_BORROWER'])
@GrailsCompileStatic
class MyLibraryController implements WebAttributes {
    TaackUiService taackUiService
    MyLibraryUiService myLibraryUiService
    TaackSaveService taackSaveService
    SpringSecurityService springSecurityService

    User currentUser
    boolean isAdmin = false

    /*------------------------------------------------------------*/
    /* General actions                                            */
    /*------------------------------------------------------------*/

    /**
     * Default landing action for the controller.
     *
     * **Purpose:** Initializes user context and redirects to the list of authors as the default page.
     *
     * **How it works:**
     * - Retrieves the current user from `springSecurityService.currentUser` and checks if they have admin privileges.
     * - Redirects to the `listAuthor` action.
     *
     * **Outputs:** Redirects to the listAuthor view.
     */
    def index() {
        currentUser = springSecurityService.currentUser as User
        isAdmin = currentUser?.authorities?.any { it.authority == 'ROLE_ADMIN' }
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
            tableFilter filterAuthorSpecifier, tableAuthorSpecifier, {
                menuIcon ActionIcon.CREATE, this.&createAuthor as MethodClosure
            }
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
    // TODO 6.1.1: Add @Secured(['ROLE_ADMIN'])
    def createAuthor(MyLibraryAuthor author) {
        UiFormSpecifier formAuthorSpecifier = myLibraryUiService.buildAuthorForm(author)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form formAuthorSpecifier
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
    // TODO 6.1.2: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteAuthor(MyLibraryAuthor author) {
        author.isActive = false
        redirect action: 'listAuthor'
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
    // TODO 6.1.3: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def activateAuthor(MyLibraryAuthor author) {
        author.isActive = true
        redirect action: 'listAuthor'
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
    // TODO 6.1.4: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def saveAuthor() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryAuthor)
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
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable(author)
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()
        UiShowSpecifier showAuthorSpecifier = myLibraryUiService.buildAuthorShow(author)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showAuthorSpecifier
                tableFilter filterBookSpecifier, tableBookSpecifier
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
    // TODO 6.1.5: Add @Secured(['ROLE_ADMIN'])
    def selectAuthor() {
        UiTableSpecifier tableAuthorSpecifier = myLibraryUiService.buildAuthorTable(true)
        UiFilterSpecifier filterAuthorSpecifier = myLibraryUiService.buildAuthorFilter()
        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                tableFilter filterAuthorSpecifier, tableAuthorSpecifier
            }
        })
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
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable()
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterBookSpecifier, tableBookSpecifier, {
                menuIcon ActionIcon.CREATE, this.&createBook as MethodClosure
            }
        }, myLibraryUiService.buildMenu())
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
    // TODO 6.1.6: Add @Secured(['ROLE_ADMIN'])
    def createBook(MyLibraryBook book) {
        UiFormSpecifier tableFormSpecifier = myLibraryUiService.buildBookForm(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableFormSpecifier
            }
        }
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
    // TODO 6.1.7: Add @Secured(['ROLE_ADMIN'])
    def purchaseBook(MyLibraryBook book) {
        UiFormSpecifier tableAddBookInstanceSpecifier = myLibraryUiService.buildBookPurchase(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableAddBookInstanceSpecifier
            }
        }
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
    // TODO 6.1.8: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def purchaseAndSaveBook(NumberForInstances numberForInstances, MyLibraryBook book) {
        for (int i = 0; i < (numberForInstances.numberOfInstances) as Integer; i++) {
            MyLibraryBookInstance newBookInstance = new MyLibraryBookInstance()
            newBookInstance.book = book
            book.addToListOfBookInstance(newBookInstance)
        }
        taackUiService.ajaxReload()
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
    // TODO 6.1.9: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def saveBook() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryBook)
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
        UiShowSpecifier showBookSpecifier = myLibraryUiService.buildBookShow(book)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showBookSpecifier
            }
        })
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
    // TODO 6.1.10: Add @Secured(['ROLE_ADMIN'])
    def selectBookInstance(MyLibraryBook book) {
        UiTableSpecifier bookInstanceTableSpecifier = myLibraryUiService.buildInstanceBookTable(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal true, {
                table bookInstanceTableSpecifier
            }
        }
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
    // TODO 6.1.11: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteBookInstances(MyLibraryBookInstance bookInstance) {
        MyLibraryBook book = MyLibraryBook.get(params.long('bookId'))
        UiTableSpecifier bookInstanceTable = myLibraryUiService.buildInstanceBookTable(book)

        bookInstance.isActive = false
        bookInstance.save(flush: true, validate: false)

        taackUiService.show new UiBlockSpecifier().ui {
            closeModalAndUpdateBlock {
                tableFilter(
                        myLibraryUiService.buildBookFilter(),
                        myLibraryUiService.buildBookTable(),
                ) {
                    menuIcon ActionIcon.CREATE, this.&createBook as MethodClosure
                }
                modal {
                    table bookInstanceTable
                }
            }
        }
    }

    /**
     * Opens a modal form for requesting to borrow a specific book.
     *
     * **Purpose:** Displays the book request form inside a modal window, allowing users to submit their borrow requests.
     *
     * **How it works (to implement):**
     * - Build the request book form specifier by calling `myLibraryUiService.buildRequestBookForm(book)`.
     * - Use `taackUiService.show` to render a modal containing the form.
     *
     * **Inputs:**
     * - `book`: The book the user wants to request.
     *
     * **Outputs:** Displays the modal request form to the user.
     */
    // TODO 6.1.12: Add @Secured(['ROLE_BORROWER'])
    def requestBookInstance(MyLibraryBook book){
        UiFormSpecifier requestBookInstanceForm = myLibraryUiService.buildRequestBookForm(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form requestBookInstanceForm
            }
        }
    }

    /**
     * Opens a modal displaying a table of book instances for a specific book, allowing the user to select one instance.
     *
     * **Purpose:** Provides a selection interface for choosing a single available book instance, typically used in forms where the user needs to select which specific copy they want to borrow.
     *
     * **How it works (to implement):**
     * - Builds the book instance table specifier by calling `myLibraryUiService.buildInstanceBookTable(book, true)`, enabling selection mode.
     * - Uses `taackUiService.show` to render a modal containing the table for selection.
     *
     * **Inputs:**
     * - `book`: The book whose instances are to be listed for selection.
     *
     * **Outputs:** Displays a modal with a selectable table of book instances.
     */
    // TODO 6.1.13: Add @Secured(['ROLE_BORROWER'])
    def selectBookInstanceOne(MyLibraryBook book) {
        UiTableSpecifier bookInstanceTable = myLibraryUiService.buildInstanceBookTable(book, true)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                table bookInstanceTable
            }
        }
    }

    /**
     * Saves a new book borrow request and marks the selected book instance as unavailable.
     *
     * **Purpose:** Persists the borrow request in the database and updates the availability status of the selected book instance.
     *
     * **How it works (to implement):**
     * - Save the `MyLibraryBorrowed` object using `taackSaveService.save`.
     * - If a book instance is associated with the borrow record, set its `isAvailableB` property to false to mark it as unavailable.
     * - Use `taackSaveService.redirectOrRenderErrors` to redirect if saving succeeded or re-render the form with validation errors if it failed.
     *
     * **Inputs:** None directly; uses request parameters bound to MyLibraryBorrowed.
     *
     * **Outputs:** Saves the borrow request, updates availability status, and redirects or renders errors accordingly.
     */
    // TODO 6.1.14: Add @Secured(['ROLE_BORROWER'])
    @Transactional
    def saveBookForm() {
        MyLibraryBorrowed borrowed = taackSaveService.save(MyLibraryBorrowed)
        borrowed.bookInstance?.isAvailableB = false
        taackSaveService.redirectOrRenderErrors(borrowed)
    }

    /*------------------------------------------------------------*/
    /* Borrowed menu                                              */
    /*------------------------------------------------------------*/

    /**
     * Displays a table of books that are currently borrowed, along with a filter bar.
     *
     * **Purpose:** Allows users or librarians to view all books currently borrowed, with filtering options.
     *
     * **How it works (to implement):**
     * - Build the table specifier for currently borrowed books by calling `myLibraryUiService.buildUserBorrowsTable(true)`.
     * - Build the filter specifier for borrowed books by calling `myLibraryUiService.buildUserBorrowsFilter()`.
     * - Use `taackUiService.show` to render a UI block containing:
     *   - A tableFilter combining the filter and table specifiers.
     * - Include the general menu by calling `myLibraryUiService.buildMenu()`.
     *
     * **Inputs:** None directly; uses service methods to build UI components.
     *
     * **Outputs:** Renders the current borrowings table with a filter bar.
     */
    // TODO 6.1.15: Add @Secured(['ROLE_BORROWER'])
    def listBooksCurrentlyBorrowed() {
        UiTableSpecifier tableUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable(true)
        UiFilterSpecifier filterUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterUserBorrowsSpecifier, tableUserBorrowsSpecifier
        }, myLibraryUiService.buildMenu())
    }

    /**
     * Opens a modal form to record the return of a borrowed book.
     *
     * **Purpose:** Displays the return form in a modal window, allowing users or librarians to input the return date when a borrowed book is returned.
     *
     * **How it works (to implement):**
     * - Builds the return book form specifier by calling `myLibraryUiService.buildRequestReturnBookForm(borrowed)`.
     * - Uses `taackUiService.show` to render a modal containing the return form.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record for which the return is being recorded.
     *
     * **Outputs:** Displays the modal return form to the user.
     */
    // TODO 6.1.16: Add @Secured(['ROLE_BORROWER'])
    def returnBook(MyLibraryBorrowed borrowed) {
        UiFormSpecifier requestReturnBookInstanceForm = myLibraryUiService.buildRequestReturnBookForm(borrowed)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form requestReturnBookInstanceForm
            }
        }
    }

    /**
     * Saves the return of a borrowed book and marks the book instance as available again.
     *
     * **Purpose:** Persists the return date for a borrow record and updates the availability status of the associated book instance to indicate it can be borrowed again.
     *
     * **How it works (to implement):**
     * - Saves the `MyLibraryBorrowed` object using `taackSaveService.save`.
     * - If a book instance is associated with the borrow record, sets its `isAvailableB` property to true to mark it as available.
     * - Calls `taackSaveService.redirectOrRenderErrors` with the saved borrow record to handle UI redirection or re-rendering with errors.
     *
     * **Inputs:** None directly; uses request parameters bound to MyLibraryBorrowed.
     *
     * **Outputs:** Saves the return information, updates the book instance availability, and redirects or renders errors accordingly.
     */
    // TODO 6.1.17: Add @Secured(['ROLE_BORROWER'])
    @Transactional
    def saveReturnBookForm() {
        MyLibraryBorrowed borrowed = taackSaveService.save(MyLibraryBorrowed)
        borrowed.bookInstance?.isAvailableB = true
        taackSaveService.redirectOrRenderErrors(borrowed)
    }

    /*------------------------------------------------------------*/
    /* History menu                                               */
    /*------------------------------------------------------------*/

    /**
     * Displays a table of all books that have been borrowed in the past, along with a filter bar.
     *
     * **Purpose:** Allows users or librarians to view all past borrow records with filtering options.
     *
     * **How it works (to implement):**
     * - Builds the table specifier for borrowed books by calling `myLibraryUiService.buildUserBorrowsTable()` without the `isCurrently` flag, showing past borrowings.
     * - Builds the filter specifier for borrowed books by calling `myLibraryUiService.buildUserBorrowsFilter()`.
     * - Uses `taackUiService.show` to render a UI block containing:
     *   - A tableFilter combining the filter and table specifiers.
     * - Includes the general menu built by `myLibraryUiService.buildMenu()`.
     *
     * **Inputs:** None directly; uses service methods to build UI components.
     *
     * **Outputs:** Renders the borrow records table with a filter bar for past borrowings.
     */
    // TODO 6.1.18: Add @Secured(['ROLE_BORROWER'])
    def listBooksBorrowed() {
        UiTableSpecifier tableUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable()
        UiFilterSpecifier filterUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterUserBorrowsSpecifier, tableUserBorrowsSpecifier
        }, myLibraryUiService.buildMenu())
    }

    /**
     * Opens a modal displaying the details of a borrow record in read-only format.
     *
     * **Purpose:** Allows users or librarians to view all information related to a specific borrowed book record in a non-editable modal window.
     *
     * **How it works (to implement):**
     * - Builds the show specifier for the borrow record by calling `myLibraryUiService.buildBorrowedShow(borrowed)`.
     * - Uses `taackUiService.show` to render a modal containing the show specifier.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record whose details are to be displayed.
     *
     * **Outputs:** Displays the modal showing the borrow record's details.
     */
    def showBorrowed(MyLibraryBorrowed borrowed) {
        UiShowSpecifier showSpec = myLibraryUiService.buildBorrowedShow(borrowed)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showSpec
            }
        })
    }

    /*------------------------------------------------------------*/
    /* Requests menu                                              */
    /*------------------------------------------------------------*/

    /**
     * Opens a modal form to approve a book borrow request.
     *
     * **Purpose:** Displays the approval form in a modal window, allowing librarians to set the approval date and status for a borrow request.
     *
     * **How it works (to implement):**
     * - Builds the approval form specifier by calling `myLibraryUiService.buildApproveBookTable(borrowed)`.
     * - Uses `taackUiService.show` to render a modal containing the approval form.
     *
     * **Inputs:**
     * - `borrowed`: The borrow record being approved.
     *
     * **Outputs:** Displays the modal approval form to the user.
     */
    // TODO 6.1.19: Add @Secured(['ROLE_ADMIN'])
    def approveBook(MyLibraryBorrowed borrowed) {
        UiFormSpecifier approveBookSpecifier = myLibraryUiService.buildApproveBookTable(borrowed)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form approveBookSpecifier
            }
        })
    }

    /**
     * Saves the approval status of a book borrow request and updates its return date if rejected.
     *
     * **Purpose:** Persists approval decisions for borrow requests, and if the request is rejected, sets a placeholder far-future return date.
     *
     * **How it works (to implement):**
     * - Creates a `Calendar` instance and sets its date to December 31, year 999999 as a placeholder.
     * - Saves the `MyLibraryBorrowed` object using `taackSaveService.save`.
     * - Checks if the approval status is `REJECTED`:
     *   - If so, sets the borrow record's `returnDate` to the far-future placeholder date.
     * - Calls `taackSaveService.redirectOrRenderErrors` with the saved borrow record to handle UI redirection or re-rendering with errors.
     *
     * **Inputs:** None directly; uses request parameters bound to MyLibraryBorrowed.
     *
     * **Outputs:** Saves the approval decision, updates return date if rejected, and redirects or renders errors accordingly.
     */
    // TODO 6.1.20: Add @Secured(['ROLE_ADMIN'])
    @Transactional
    def saveApprovalBookForm() {
        Calendar cal = Calendar.getInstance()
        cal.set(999999, Calendar.DECEMBER, 31)
        Date date = cal.time
        MyLibraryBorrowed borrowed = taackSaveService.save(MyLibraryBorrowed)
        if(borrowed.statusOfApproval == ApprovalStatus.REJECTED) {
            borrowed.returnDate = date
        }
        taackSaveService.redirectOrRenderErrors(borrowed)
    }

    /**
     * Displays a table of all current book borrow requests in the library system.
     *
     * **Purpose:** Allows admins to view and manage all active borrow requests from users.
     *
     * **How it works (to implement):**
     * - Builds the table specifier for current borrow requests by calling `myLibraryUiService.buildUserBorrowsTable(true, null, true)`.
     * - Builds the filter specifier for borrow requests by calling `myLibraryUiService.buildUserBorrowsFilter()`.
     * - Uses `taackUiService.show` to render a UI block containing:
     *   - A tableFilter combining the filter and table specifiers.
     *   - The general menu for navigation.
     *
     * **Inputs:** None directly; uses service methods to build UI components for active borrow requests.
     *
     * **Outputs:** Renders the table of current borrow requests with a filter bar and navigation menu.
     */
    // TODO 6.1.21: Add @Secured(['ROLE_ADMIN'])
    def listOfRequests() {
        // TODO 4.3.8: Build tableUserBorrowsSpecifier by calling myLibraryUiService.buildUserBorrowsTable(true, null, true).
        // TODO 4.3.9: Build filterUserBorrowsSpecifier by calling myLibraryUiService.buildUserBorrowsFilter().
        // TODO 4.3.10: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 4.3.11: Add tableFilter combining filterUserBorrowsSpecifier and tableUserBorrowsSpecifier.
        // TODO 4.3.12: Include the general menu by calling myLibraryUiService.buildMenu().
    }

    /*------------------------------------------------------------*/
    /* Users menu                                              */
    /*------------------------------------------------------------*/

    /**
     * Displays the list of all users in the system with their username and authorities.
     *
     * **Purpose:** Allows admins to view and manage registered users.
     *
     * **Outputs:** Renders the users table with a navigation menu.
     */
    // TODO 6.1.22: Add @Secured(['ROLE_ADMIN'])
    def listOfUsers() {
        // TODO 5.2.1: Build tableUsersSpecifier by calling myLibraryUiService.buildUsersTable().
        // TODO 5.2.2: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 5.2.3: Add table using tableUsersSpecifier.
        // TODO 5.2.4: Include the general menu by calling myLibraryUiService.buildMenu().
    }

    /**
     * Displays a modal showing user details along with their current and past borrow records.
     *
     * **Purpose:** Allows admins to view a user's information and their borrowing history in a single modal.
     *
     * **Inputs:**
     * - `user`: The user whose details and borrow records are to be displayed.
     *
     * **Outputs:** Renders the user detail view and both borrow tables inside a modal.
     */
    // TODO 6.1.23: Add @Secured(['ROLE_ADMIN'])
    def showUser(User user) {
        // TODO 5.4.1: Build showSpec by calling myLibraryUiService.buildUserShow(user).
        // TODO 5.4.2: Build userBorrowsSpecifier by calling myLibraryUiService.buildUserBorrowsTable(false, user).
        // TODO 5.4.3: Build userBorrowsFilterSpecifier by calling myLibraryUiService.buildUserBorrowsFilter(user).
        // TODO 5.4.4: Build userBorrowsCurrentlySpecifier by calling myLibraryUiService.buildUserBorrowsTable(true, user).

        // TODO 5.4.5: Use taackUiService.show to render a UiBlockSpecifier.
        // Inside show block:
        // - TODO 5.4.6: Define a modal block.
        //   - TODO 5.4.7: Add show using showSpec.
        //   - TODO 5.4.8: Add tableFilter combining userBorrowsFilterSpecifier and userBorrowsSpecifier.
        //   - TODO 5.4.9: Add tableFilter combining userBorrowsFilterSpecifier and userBorrowsCurrentlySpecifier.
    }

}