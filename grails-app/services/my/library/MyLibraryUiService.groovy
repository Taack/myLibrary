package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
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


@GrailsCompileStatic
class MyLibraryUiService implements WebAttributes {
    TaackFilterService taackFilterService
    SpringSecurityService springSecurityService




    @PostConstruct
    void init() {
        TaackAppRegisterService.register(new TaackApp(MyLibraryController.&index as MC, new String(this.class.getResourceAsStream("/myLibrary/library-svgrepo-com.svg").readAllBytes())))
    }

    boolean isAdmin() {
        User currentUser = springSecurityService.currentUser as User
        return currentUser?.authorities?.any { it.authority == 'ROLE_ADMIN' }
    }

    /*--------------- General Menu --------------------------------*/

    UiMenuSpecifier buildMenu() {
        UiMenuSpecifier m = new UiMenuSpecifier()
        m.ui {

            menu MyLibraryController.&listBook as MC
            menu MyLibraryController.&index as MC
            menu MyLibraryController.&listBooksBorrowed as MC
            menu MyLibraryController.&listBooksCurrentlyBorrowed as MC
            menu MyLibraryController.&listOfUsers as MC
            menu MyLibraryController.&listOfRequests as MC

        }
    }


    /*--------------- Author Menu --------------------------------*/

    //Active filter (checks if the author is active of not)
    UiFilterSpecifier buildIsActiveAuthorFilter(MyLibraryAuthor author) {
        UiFilterSpecifier isActiveAuthorFilter = new UiFilterSpecifier()
        isActiveAuthorFilter.ui MyLibraryAuthor, {
            section "Filter", {
                filterFieldExpressionBool "Is Active", new FilterExpression(true, Operator.EQ, author.isActive_)
            }
        }
    }

    //Table display (displays a table with authors according to the users credentials)
    UiTableSpecifier buildAuthorTable(Boolean isSelect = false) {
        boolean isAdmin = isAdmin()


        MyLibraryAuthor author = new MyLibraryAuthor()
        UiTableSpecifier authorTableSpecifier = new UiTableSpecifier()

        authorTableSpecifier.ui {
            header {
                column {label author.firstName_}
                label author.lastName_
                if(!isSelect) {
                    if(isAdmin) {
                        label author.isActive_
                        label "Delete Author" //only for ADMIN
                    }
                }
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryAuthor)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, author.lastName_)
            if(!isAdmin) {
                filter.addFilter(buildIsActiveAuthorFilter(author)) //for admin remove
            }

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
                    if(isAdmin) {
                        rowField authorIterator.isActive_ //for borrower remove
                        rowColumn {
                            rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteAuthor as MC, authorIterator.id
                            rowAction ActionIcon.CREATE * IconStyle.SCALE_DOWN, MyLibraryController.&activateAuthor as MC, authorIterator.id
                            //only for ADMIN
                        }
                    }
                }
            }
        }
    }

    //Form display (displays a from to create an author)
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

    //Filter Display (displays a filter to filter the authors according to name)
    UiFilterSpecifier buildAuthorFilter() {
        MyLibraryAuthor author = new MyLibraryAuthor()
        UiFilterSpecifier authorFilterSpecifier = new UiFilterSpecifier()

        authorFilterSpecifier.ui MyLibraryAuthor, {
            section "Author Filter", {
                filterField author.lastName_
            }
        }
    }


    /*--------------- Book Menu --------------------------------*/

    //isAvailable filter display (filter to check if there are book instances available)
    UiFilterSpecifier buildIsAvailableBookFilter(MyLibraryBook book) {
        UiFilterSpecifier isAvailableBookFilter = new UiFilterSpecifier()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        isAvailableBookFilter.ui MyLibraryBook, {
            section "Filter", {
                filterFieldExpressionBool "Is Available", new FilterExpression(true, Operator.EQ, book.listOfBookInstance_,bookInstance.isAvailableB_)
            }
        }
    }


    //isActive fitler display (filter to check if the book instances are active
    UiFilterSpecifier buildIsActiveBookFilter(MyLibraryBook book) {
        UiFilterSpecifier isActiveBookFilter = new UiFilterSpecifier()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        isActiveBookFilter.ui MyLibraryBook, {
            section "Filter", {
                filterFieldExpressionBool "Is Active", new FilterExpression(true, Operator.EQ, book.listOfBookInstance_,bookInstance.isActive_)
            }
        }
    }

    //Table display (displays the books in a table)
    UiTableSpecifier buildBookTable(MyLibraryAuthor author = null) {
        boolean isAdmin = isAdmin()

        MyLibraryBook book = new MyLibraryBook()
        UiTableSpecifier bookTableSpecifier = new UiTableSpecifier()
        bookTableSpecifier.ui {
            header {
                column {label book.title_}
                if (!author) {sortableFieldHeader book.author_}
                if(isAdmin) {
                    column {
                        label "Number of instances "//book.numberOfInstances
                    }
                }
                if (!author) {
                    label "Number of Available Book Instances"
                    if(isAdmin) {
                        column {
                            label "Modify number of Book Instances" //only for ADMIN
                        }
                    } else {
                        column {
                            label "Request Form" //only for BORROWERS
                        }
                    }
                }
            }
            TaackFilter.FilterBuilder filter =  taackFilterService.getBuilder(MyLibraryBook)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, book.title_)
//                    .addFilter(buildIsActiveBookFilter(book)) //check if isActive
            if(!isAdmin) {
                   filter.addFilter(buildIsAvailableBookFilter(book)) //for admin remove
            }
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
                if (isAdmin) {
                    rowColumn {
                        bookIterator.getNumberOfInstances()
                        rowField bookIterator.numberOfInstances_  //for borrower remove
                    }
                }
                if (!author) {
                    rowField bookIterator.numberOfBooksBorrowable_
                    if(isAdmin) {
                        rowColumn {
                            rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&selectBookInstance as MC, bookIterator.id
                            rowAction ActionIcon.ADD * IconStyle.SCALE_DOWN, MyLibraryController.&purchaseBook as MC, bookIterator.id
                        }
                    } else {
                        rowColumn {
                            rowAction ActionIcon.CREATE * IconStyle.SCALE_DOWN, MyLibraryController.&requestBookInstance as MC, bookIterator.id
                        }
                    }
                }
            }
        }
    }

    //From display (displays a from to add books)
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

    //Filter display (filters according to tile books)
    UiFilterSpecifier buildBookFilter() {
        MyLibraryBook book = new MyLibraryBook()
        UiFilterSpecifier bookFilterSpecifier = new UiFilterSpecifier()

        bookFilterSpecifier.ui MyLibraryBook, {
            section "Book Filter", {
                filterField book.title_
            }
        }
    }

    //Filter to select the book Instances that are available
    UiFilterSpecifier buildIsAvailableBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isAvailableB_)    //new FilterExpression(true, Operator.EQ, bookInstance.isAvailable_)
        }
    }

    //Filter to select the book Instances that are Active
    UiFilterSpecifier buildIsActiveBookInstances(MyLibraryBook book) {
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()
        UiFilterSpecifier bookInstanceFilterSpecifier = new UiFilterSpecifier()
        bookInstanceFilterSpecifier.sec MyLibraryBookInstance, {
            filterFieldExpressionBool new FilterExpression(true, Operator.EQ, bookInstance.isActive_)    //new FilterExpression(true, Operator.EQ, bookInstance.isAvailable_)
        }
    }

    //Form for purchase display
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

    //Table (displays all the bookInstances of a book in a table)
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
            filter.addFilter(buildIsAvailableBookInstances(book))

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

    //Request book Form for request to borrow display
    UiFormSpecifier buildRequestBookForm(MyLibraryBook book) {
        User user = springSecurityService.currentUser as User

        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        borrowed.user = user


        book ?= new MyLibraryBook(params)
        UiFormSpecifier requestBookFormSpecifier = new UiFormSpecifier()

        //MyLibraryBorrowed borrowed = MyLibraryBorrowed.findByUser(user)

        requestBookFormSpecifier.ui borrowed, {
            section "Request Book Form", {
                hiddenField borrowed.user_ //pass paramretes in the form witout the user seeing
                field borrowed.requestDate_
                ajaxField borrowed.bookInstance_, MyLibraryController.&selectBookInstanceOne as MC, book.id

            }
            formAction MyLibraryController.&requestBookForm as MC
        }
    }

    /*--------------- History & Borrowing Menu --------------------------------*/

    UiTableSpecifier buildUserBorrowsTable(isCurrently = false, User showUser = null, isUser = false) {
        Boolean isAdmin = isAdmin()
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
                if (isCurrently && !isAdmin) {
                    column {
                        label "Return Book"
                    }
                }
                if(isAdmin) {
                    column {
                        label borrowed.user_
                    }
                    if (!showUser) {
                        label "Approve Book"
                    }
                }
            }


            User currentUser = springSecurityService.currentUser as User
            if (showUser) {
                currentUser = showUser
            }

            TaackFilter.FilterBuilder filter = taackFilterService.getBuilder(MyLibraryBorrowed)
                    .setMaxNumberOfLine(10)
                    .setSortOrder(TaackFilter.Order.ASC, borrowed.bookInstance_,bookInstance.book_,book.title_)//borrowed.bookInstance_) //add book.title_     borrowed.bookInstance_,book.title_

            if(!isUser || showUser) {
                filter.addFilter(new FilterExpression(currentUser, Operator.EQ, borrowed.user_)) //check if this is right
            }
            if(isCurrently) { //no return date
                filter.addFilter(new FilterExpression(null, Operator.EQ, borrowed.returnDate_))
            } else { //has return date
                filter.addFilter(new FilterExpression(null, Operator.NE, borrowed.returnDate_))
            }

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
                if(isCurrently && !isAdmin && borrowedIterator.approvalDate_) {
                    rowColumn {
                        rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&
                                returnBook as MC, borrowedIterator.id
                    }
                }
                if(isAdmin) {
                    rowColumn {
                        rowField borrowedIterator.user.username_
                    }
                    if(!showUser) {
                        rowColumn {
                            rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&approveBook as MC, borrowedIterator.id
                        }
                    }
                }
            }
        }
    }

    UiFilterSpecifier buildUserBorrowsFilter() {
        MyLibraryBook book = new MyLibraryBook()
        UiFilterSpecifier UserBorrowsFilterSpecifier = new UiFilterSpecifier()
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        MyLibraryBookInstance bookInstance = new MyLibraryBookInstance()

        UserBorrowsFilterSpecifier.ui MyLibraryBorrowed, {
            section "Borrows Filter", {
                filterField borrowed.bookInstance_,bookInstance.book_,book.title_
            }
        }
    }


    //make in one
    UiFormSpecifier buildRequestReturnBookForm(MyLibraryBorrowed borrowed) {
        UiFormSpecifier requestBookFormSpecifier = new UiFormSpecifier()


        requestBookFormSpecifier.ui borrowed, {
            section "Request Return Book Form", {
                field borrowed.returnDate_
            }
            formAction MyLibraryController.&requestReturnBookForm as MC   //save book form
        }
    }


    /*--------------- Users Menu --------------------------------*/

    UiTableSpecifier buildUsersTable() {
        MyLibraryBorrowed borrowed = new MyLibraryBorrowed()
        UiTableSpecifier buildUsersSpecifier = new UiTableSpecifier()
        User user = new User()

        buildUsersSpecifier.ui {
            header {
                label user.username_
                label "Authorities"
//                label "Preview User"
                label "Deactivate User"
            }

            TaackFilter taackFilter = taackFilterService.getBuilder(User)
                    .setSortOrder(TaackFilter.Order.ASC, user.username_)
                    .setMaxNumberOfLine(10).build()
//                    .addFilter(new FilterExpression(currentUser, Operator.EQ, borrowed.user_)) //check if this is right
//                    .addRestrictedIds()

            iterate taackFilter, {User userIterator ->
                rowColumn {
                    rowAction ActionIcon.SHOW * IconStyle.SCALE_DOWN, MyLibraryController.&showUser as MC, userIterator.id
                    rowField userIterator.username_
                }
                rowField userIterator.authorities_
                rowColumn {
                    rowAction ActionIcon.DELETE * IconStyle.SCALE_DOWN, MyLibraryController.&deleteUser as MC, userIterator.id
                }
            }
        }
    }

    UiFormSpecifier buildApproveBookTable(MyLibraryBorrowed borrowed) {
        UiFormSpecifier approveBookSpecifier = new UiFormSpecifier()

        approveBookSpecifier.ui borrowed, {
            section "Approve Book Form", {
                field borrowed.approvalDate_
                field borrowed.statusOfApproval_
            }
            formAction MyLibraryController.&saveApprovalBookForm as MC   //save book form
        }

    }

 //add filter
}

