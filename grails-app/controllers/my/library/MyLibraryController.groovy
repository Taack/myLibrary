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

@GrailsCompileStatic
class MyLibraryController implements WebAttributes {
    TaackUiService taackUiService
    MyLibraryUiService myLibraryUiService
    TaackSaveService taackSaveService

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
     * Builds and displays the *Author* table together with a filter bar. A
     * *Create* icon is added to the table header that opens the {@link #createAuthor(MyLibraryAuthor)}
     * modal.
     */
    def listAuthor() {
        UiTableSpecifier tableAuthorSpecifier = myLibraryUiService.buildAuthorTable(false)
        UiFilterSpecifier filterAuthorSpecifier = myLibraryUiService.buildAuthorFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterAuthorSpecifier, tableAuthorSpecifier, {
                menuIcon ActionIcon.CREATE, this.&createAuthor as MethodClosure
            }
        }, myLibraryUiService.buildMenu())
    }

    /**
     * Opens a modal form (built by {@link MyLibraryUiService#buildAuthorForm(MyLibraryAuthor)})
     * to create or edit an author.
     *
     * @param author optional existing author – will be pre‑filled when editing
     */
    def createAuthor(MyLibraryAuthor author) {
        UiFormSpecifier formAuthorSpecifier = myLibraryUiService.buildAuthorForm(author)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form formAuthorSpecifier
            }
        })
    }

    /**
     * Soft‑deletes the given author by setting {@code isActive = false}. The
     * record remains in the database for auditing purposes.
     *
     * @param author the author to deactivate
     */
    @Transactional
    def deleteAuthor(MyLibraryAuthor author) {
        author.isActive = false
        redirect action: 'listAuthor'
    }

    /**
     * Reactivates a previously deactivated author.
     *
     * @param author the author to reactivate
     */
    @Transactional
    def activateAuthor(MyLibraryAuthor author) {
        //TODO: reactivate the author using isActive then redirect to index.
        author.isActive = true
        redirect action: 'listAuthor'
    }

    /**
     * Persists a new or edited author and reloads the page, or re‑renders the
     * form with validation errors if saving fails. Delegated to
     * {@link TaackSaveService}.
     */
    @Transactional
    def saveAuthor() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryAuthor)
    }

    /**
     * Shows a read‑only detail view for the selected author, followed by the
     * list of books written by that author.
     *
     * @param author the author whose details are to be displayed
     */
    def showAuthor(MyLibraryAuthor author) {
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable(author)
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()
        UiShowSpecifier showBookSpecifier = new UiShowSpecifier()

        showBookSpecifier.ui(author, {
            fieldLabeled author.firstName_
            fieldLabeled author.lastName_
            fieldLabeled author.dateOfBirth_
            fieldLabeled author.isActive_
        })
        //TODO: put this in the ui services for persistency purposes ??

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showBookSpecifier
                tableFilter filterBookSpecifier, tableBookSpecifier
            }
        })
    }

    /**
     * Renders a modal containing a filterable/selectable author table so the
     * caller can pick an author and have its ID returned via AJAX.
     */
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
     * Shows all books in a table with an accompanying filter bar. Includes a
     * *Create* icon that opens the {@link #createBook(MyLibraryBook)} modal.
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
     * Opens a modal for creating or editing a book. The actual form is provided
     * by {@link MyLibraryUiService#buildBookForm(MyLibraryBook)}.
     *
     * @param book optional existing book for edit mode
     */
    def createBook(MyLibraryBook book) {
        UiFormSpecifier tableFormSpecifier = myLibraryUiService.buildBookForm(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableFormSpecifier
            }
        }
    }

    /**
     * Opens a modal form that lets the librarian specify how many physical
     * copies of a given book to purchase (i.e. create). Internally delegates to
     * {@link MyLibraryUiService#buildBookPurchase(MyLibraryBook)}.
     */
    def purchaseBook(MyLibraryBook book) {
        UiFormSpecifier tableAddBookInstanceSpecifier = myLibraryUiService.buildBookPurchase(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableAddBookInstanceSpecifier
            }
        }
    }

    /**
     * Creates the requested number of {@link MyLibraryBookInstance}s for the
     * specified book and reloads the surrounding UI block via AJAX.
     *
     * @param numberForInstances wrapper containing the desired number of copies
     * @param book               the logical book entity to which copies belong
     */
    @Transactional
    def purchaseAndSaveBook(NumberForInstances numberForInstances, MyLibraryBook book) {
        for (int i = 0; i < (numberForInstances.numberOfInstances) as Integer; i++) {
            MyLibraryBookInstance newBookInstance = new MyLibraryBookInstance()
            newBookInstance.book = book
            book.addToListOfBookInstance(newBookInstance)
        }
        taackUiService.ajaxReload()
    }


    //TODO implement or erase
    /**
     * Soft‑deletes a book to let the user select specific instances to delete.
     */
    @Transactional
    def deleteBook(MyLibraryBook book) {
        //create a from to select the bookInstances to delete
        //book.isActive = false
        redirect action: 'listBook'
    }

    /**
     * Saves a book entity (new or edited) and either reloads the page or
     * re‑renders the form with validation errors.
     */
    @Transactional
    def saveBook() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryBook)
    }

    /**
     * Displays read‑only details of a single book inside a modal.
     *
     * @param book the book whose details are requested
     */
    def showBook(MyLibraryBook book) {
        UiShowSpecifier showBookSpecifier = new UiShowSpecifier().ui(book, {
            fieldLabeled book.title_
            fieldLabeled book.author_
            fieldLabeled book.numberOfPages_
            fieldLabeled book.description_
            fieldLabeled book.numberOfInstances_
        })

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showBookSpecifier
            }
        })
    }

    /**
     * Opens a modal selector listing all books.
     */
    def selectBook() {
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable()
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()
        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                tableFilter filterBookSpecifier, tableBookSpecifier
            }
        }
    }

    /**
     * Opens a modal that shows every physical instance of a given book. The
     * list is filterable and selectable so the librarian can pick the exact
     * copy to lend or delete.
     */
    def selectBookInstance(MyLibraryBook book) {
        UiTableSpecifier bookInstanceTableSpecifier = myLibraryUiService.buildInstanceBookTable(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal true, {
                table bookInstanceTableSpecifier
            }
        }
    }

    /**
     * Soft‑deletes (deactivates) a single physical book instance and refreshes
     * the surrounding tables to reflect the change.
     *
     * @param bookInstance the specific physical copy to deactivate
     */
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

}