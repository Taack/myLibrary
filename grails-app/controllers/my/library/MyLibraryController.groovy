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

@GrailsCompileStatic
@Secured(['ROLE_ADMIN','ROLE_BORROWER'])
class MyLibraryController implements WebAttributes {
    TaackUiService taackUiService
    MyLibraryUiService myLibraryUiService
    TaackSaveService taackSaveService
    SpringSecurityService springSecurityService
    User currentUser
    boolean isAdmin = false


    //test

    def index() {
        currentUser = springSecurityService.currentUser as User
        isAdmin = currentUser?.authorities?.any { it.authority == 'ROLE_ADMIN' }
        redirect action: 'listAuthor'
    }

    /*--------------- Author Menu --------------------------------*/



    def listAuthor() {
        UiTableSpecifier tableAuthorSpecifier = myLibraryUiService.buildAuthorTable(false)
        UiFilterSpecifier filterAuthorSpecifier = myLibraryUiService.buildAuthorFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterAuthorSpecifier, tableAuthorSpecifier, {
                menuIcon ActionIcon.CREATE, this.&createAuthor as MethodClosure
                //menuIcon ActionIcon.CREATE, this.&createAuthor as MethodClosure //if isAcative = false
            }
        }, myLibraryUiService.buildMenu())
    }

    @Secured(['ROLE_ADMIN'])
    def createAuthor(MyLibraryAuthor author) {
        UiFormSpecifier formAuthorSpecifier = myLibraryUiService.buildAuthorForm author

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form formAuthorSpecifier
            }
        })
    }

    @Secured(['ROLE_ADMIN']) //ionly foor admin
    @Transactional
    def deleteAuthor(MyLibraryAuthor author) {
        author.isActive = false
        redirect action: 'listAuthor'
    }

    @Secured(['ROLE_ADMIN']) //only for admin
    @Transactional
    def activateAuthor(MyLibraryAuthor author) {
        author.isActive = true
        redirect action: 'listAuthor'
    }

    @Transactional
    def saveAuthor() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryAuthor)
    }

    def showAuthor(MyLibraryAuthor author) {

        //show book table
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable author
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()


        UiShowSpecifier showSpec = new UiShowSpecifier()

        showSpec.ui(author, {

            fieldLabeled author.firstName_
            fieldLabeled author.lastName_
            fieldLabeled author.dateOfBirth_
            fieldLabeled author.isActive_ //only for admin

        })

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showSpec
                tableFilter filterBookSpecifier, tableBookSpecifier
            }
        })
    }

    def selectAuthor() {
        UiTableSpecifier t = myLibraryUiService.buildAuthorTable true
        UiFilterSpecifier f = myLibraryUiService.buildAuthorFilter()
        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                tableFilter f, t
            }
        }
    }



    /*--------------- Book Menu --------------------------------*/

    def listBook() {
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable()
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterBookSpecifier, tableBookSpecifier, {
                menuIcon ActionIcon.CREATE, this.&createBook as MethodClosure
            }
        }, myLibraryUiService.buildMenu())
    }

    @Secured(['ROLE_ADMIN'])
    def createBook(MyLibraryBook book) {
        UiFormSpecifier tableFormSpecifier = myLibraryUiService.buildBookForm book

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableFormSpecifier
            }
        }
    }


    @Secured(['ROLE_ADMIN'])
    def purchaseBook(MyLibraryBook book) {
        UiFormSpecifier tableAddSpecifier = myLibraryUiService.buildBookPurchase book

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form tableAddSpecifier
            }
        }
    }


    @Transactional
    def purchaseAndSaveBook(NumberForInstances numberForInstances, MyLibraryBook book) {
        for (int i =0; i < (numberForInstances.numberOfInstances) as Integer; i++) {
            MyLibraryBookInstance newBookInstance = new MyLibraryBookInstance()
            newBookInstance.book = book
            //newBookInstance.save(flush: "")

            book.addToListOfBookInstance(newBookInstance)
        }
        taackUiService.ajaxReload()
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteBook(MyLibraryBook book) {
        //create a from to select the bookInstances to delete
        //book.isActive = false
        redirect action: 'listBook'
    }

    @Transactional
    def saveBook() {
        taackSaveService.saveThenReloadOrRenderErrors(MyLibraryBook)
    }

    def showBook(MyLibraryBook book) {
        UiShowSpecifier showSpec = new UiShowSpecifier().ui(book, {
            fieldLabeled book.title_
            fieldLabeled book.author_
            fieldLabeled book.numberOfPages_
            fieldLabeled book.description_
            fieldLabeled book.numberOfBooksBorrowable_
            fieldLabeled book.numberOfInstances_    //only admins
        })

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showSpec
            }
        })
    }

    def selectBook() {
        UiTableSpecifier tableBookSpecifier = myLibraryUiService.buildBookTable()
        UiFilterSpecifier filterBookSpecifier = myLibraryUiService.buildBookFilter()
        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                tableFilter filterBookSpecifier, tableBookSpecifier
            }
        }
    }

    def selectBookInstance(MyLibraryBook book) {
        UiTableSpecifier bookInstanceTable = myLibraryUiService.buildInstanceBookTable(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal true, {
                table bookInstanceTable


            }
        }
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteBookInstances(MyLibraryBookInstance bookInstance) {
        MyLibraryBook book = MyLibraryBook.get(params.long('bookId')) //get bookInstance
//        book.listOfBookInstance.remove(bookInstance)
//        bookInstance.delete(flush: true)
        bookInstance.isActive = false
//        bookInstance.isAvailableB = false
        bookInstance.save(flush: true, validate: false)
        UiTableSpecifier bookInstanceTable = myLibraryUiService.buildInstanceBookTable(book)
//
//        taackSaveService.displayBlockOrRenderErrors(bookInstance, new UiBlockSpecifier().ui {
//                closeModal(bookInstance.id, bookInstance.toString())})
//
//
//        //taackSaveService.saveThenReloadOrRenderErrors()
//        // the page is closed but not reopen reload the listBook and open the modal
//        taackUiService.show new UiBlockSpecifier().ui {
//            modal {
//                table bookInstanceTable
//            }
//        }

        taackUiService.show new UiBlockSpecifier().ui {
            closeModalAndUpdateBlock {
                tableFilter(
                        myLibraryUiService.buildBookFilter(),
                        myLibraryUiService.buildBookTable(),
                ) {
                    menuIcon ActionIcon.CREATE, this.&createBook as MethodClosure
                }

                modal {
//                    show new UiShowSpecifier().ui(null) {
//                        field "Book instance deleted successfully"
//                    }
                    table bookInstanceTable
                }
            }
        }
    }

    def requestBookInstance(MyLibraryBook book){

        UiFormSpecifier requestBookInstanceForm = myLibraryUiService.buildRequestBookForm(book)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form requestBookInstanceForm
            }
        }
    }

    def selectBookInstanceOne(MyLibraryBook book) {

        UiTableSpecifier bookInstanceTable = myLibraryUiService.buildInstanceBookTable(book, true)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                table bookInstanceTable


            }
        }
    }

    @Transactional
    def requestBookForm() {
        MyLibraryBorrowed borrowed = taackSaveService.save(MyLibraryBorrowed)
        println("--------------------------------------------------------------------------------")
        println(borrowed.user_)
        borrowed.bookInstance?.isAvailableB = false
        taackSaveService.redirectOrRenderErrors(borrowed)
    }// action with information


    /*--------------- History Menu --------------------------------*/

    @Secured(['ROLE_BORROWER'])
    def listBooksBorrowed() {
        //display a table with all the borrowed of the user that have been return
        UiTableSpecifier tableUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable()
        UiFilterSpecifier filterUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterUserBorrowsSpecifier, tableUserBorrowsSpecifier
        }, myLibraryUiService.buildMenu())
    }

    def showBorrowed(MyLibraryBorrowed borrowed) {
        //show borrwed hotristy maybe  table
//        UiTableSpecifier tableBorrowedSpecifier = myLibraryUiService.buildBorrowedTable()
//        UiFilterSpecifier filterBorrowedSpecifier = myLibraryUiService.buildBorrowedFilter()


        UiShowSpecifier showSpec = new UiShowSpecifier()

        showSpec.ui(borrowed, {

            fieldLabeled borrowed.bookInstance.book.title_
            fieldLabeled borrowed.bookInstance.book.author_
            fieldLabeled borrowed.user.username_
            fieldLabeled borrowed.statusOfApproval_
            fieldLabeled borrowed.requestDate_
            fieldLabeled borrowed.approvalDate_
            fieldLabeled borrowed.returnDate_

        })

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showSpec
//                tableFilter filterBookSpecifier, tableBookSpecifier
            }
        })
    }


    /*--------------- Borrowed Menu --------------------------------*/

    @Secured(['ROLE_BORROWER'])
    def listBooksCurrentlyBorrowed() {
        //display a table with all the currently borrowed of the user
        UiTableSpecifier tableUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable(true)
        UiFilterSpecifier filterUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterUserBorrowsSpecifier, tableUserBorrowsSpecifier
        }, myLibraryUiService.buildMenu())
    }

    def returnBook(MyLibraryBorrowed borrowed) {
        UiFormSpecifier requestReturnBookInstanceForm = myLibraryUiService.buildRequestReturnBookForm(borrowed)

        taackUiService.show new UiBlockSpecifier().ui {
            modal {
                form requestReturnBookInstanceForm
            }
        }
    }

    @Transactional
    def requestReturnBookForm() {
        MyLibraryBorrowed borrowed = taackSaveService.save(MyLibraryBorrowed)
        borrowed.bookInstance?.isAvailableB = true
        taackSaveService.redirectOrRenderErrors(borrowed)
    }


    /*--------------- Borrowers Menu --------------------------------*/

    @Secured(['ROLE_ADMIN'])
    def listOfUsers() {
        UiTableSpecifier tableUsersSpecifier = myLibraryUiService.buildUsersTable()

        taackUiService.show(new UiBlockSpecifier().ui {
            table tableUsersSpecifier
        }, myLibraryUiService.buildMenu())
    }

    def showUser(User user) {
        UiShowSpecifier showSpec = new UiShowSpecifier()

        showSpec.ui(user, {
            fieldLabeled user.username_
            fieldLabeled user.firstName_
            fieldLabeled user.lastName_
            fieldLabeled user.authorities_ //change
        })

        UiTableSpecifier userBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable(false, user)
        UiFilterSpecifier userBorrowsFilterSpecifier = myLibraryUiService.buildUserBorrowsFilter()
        UiTableSpecifier userBorrowsCurrentlySpecifier = myLibraryUiService.buildUserBorrowsTable(true, user)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                show showSpec
                tableFilter userBorrowsFilterSpecifier, userBorrowsSpecifier
                tableFilter userBorrowsFilterSpecifier, userBorrowsCurrentlySpecifier
            }
        })

    }

    @Secured(['ROLE_ADMIN'])
    def deleteUser(User user) {
        user.enabled = false //have to check if the enable changes the display of users
    }


    /*--------------- Requests Menu --------------------------------*/

    @Secured(['ROLE_ADMIN'])
    def listOfRequests() {
        UiTableSpecifier tableUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsTable(true, null, true)
        UiFilterSpecifier filterUserBorrowsSpecifier = myLibraryUiService.buildUserBorrowsFilter()

        taackUiService.show(new UiBlockSpecifier().ui {
            tableFilter filterUserBorrowsSpecifier, tableUserBorrowsSpecifier
        }, myLibraryUiService.buildMenu())
    }

    def approveBook(MyLibraryBorrowed borrowed) {
        UiFormSpecifier approveBookSpecifier = myLibraryUiService.buildApproveBookTable(borrowed)

        taackUiService.show(new UiBlockSpecifier().ui {
            modal {
                form approveBookSpecifier
            }
        })
    }

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

}




