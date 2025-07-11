package my.library

import crew.CrewController
import crew.User
import grails.plugin.springsecurity.SpringSecurityService
import jakarta.annotation.PostConstruct
import org.codehaus.groovy.runtime.MethodClosure
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.render.TaackUiEnablerService


/**
 * Provides security services for the MyLibrary application.
 *
 * **Purpose:** Defines role-based and data-based security checks to control access to controller actions in the library system. Integrates with Spring Security and Taack UI's security closure registration to enforce these checks.
 *
 * **How it works:**
 * - Uses `SpringSecurityService` to determine the current user's roles.
 * - Registers security closures for various controller actions during initialization.
 * - Defines general admin role checks as well as specific data state-based checks (e.g. approval status of borrowed books).
 *
 * **Outputs:** Enables and enforces security policies for library controller actions.
 */


class MyLibrarySecurityService {
    SpringSecurityService springSecurityService

    static lazyInit = false

    /**
     * Checks if the current user is an admin when performing security checks for general library actions.
     *
     * **Inputs:**
     * - `id`: The ID of the object being acted upon (e.g. author ID, book ID).
     * - `p`: A parameter map (not used here but passed by the security system).
     *
     * **Outputs:** Returns true if the user has admin privileges.
     *
     * **Implementation steps:**
     * - Calls `isAdmin()` to determine if the current user is an admin.
     */
    private securityClosure(Long id, Map p) {
        isAdmin()
    }

    /**
     * Retrieves the currently authenticated user from Spring Security.
     *
     * **Inputs:** None.
     *
     * **Outputs:** Returns the `User` object of the currently authenticated user.
     *
     * **Implementation steps:**
     * - Casts `springSecurityService.currentUser` to `User` and returns it.
     */
    User authenticatedRolesUser() {
        springSecurityService.currentUser as User
    }

    /**
     * Checks if the currently authenticated user has at least one of the specified roles.
     *
     * **Inputs:**
     * - `roles`: A variable-length list of role names (e.g. 'ROLE_ADMIN', 'ROLE_USER').
     *
     * **Outputs:** Returns true if the user has any of the specified roles, false otherwise.
     *
     * **Implementation steps:**
     * - Retrieves the current user.
     * - Iterates through the provided roles and checks if any are contained in the user's authorities.
     */
    boolean authenticatedRoles(String... roles) {
        User user = authenticatedRolesUser()
        for (String r : roles) {
            if (user.authorities*.authority.contains(r)) return true
        }
        return false
    }

    /**
     * Checks if the currently authenticated user has the 'ROLE_ADMIN' role.
     *
     * **Inputs:** None.
     *
     * **Outputs:** Returns true if the user is an admin, false otherwise.
     *
     * **Implementation steps:**
     * - Calls `authenticatedRoles` with 'ROLE_ADMIN' as the argument.
     */
    boolean isAdmin() {
        authenticatedRoles('ROLE_ADMIN')
    }

    /**
     * Checks if the borrowed book with the given ID has been approved.
     *
     * **Inputs:**
     * - `id`: The ID of the `MyLibraryBorrowed` record.
     * - `p`: A parameter map (not used here).
     *
     * **Outputs:** Returns true if the borrow record's status is APPROVED, false otherwise.
     *
     * **Implementation steps:**
     * - Retrieves the `MyLibraryBorrowed` object using its ID.
     * - Returns true if its `statusOfApproval` is APPROVED.
     */
    private static boolean returnSecurityClosure(Long id, Map p) {
        MyLibraryBorrowed borrowed = MyLibraryBorrowed.get(id)
        return (borrowed.statusOfApproval == ApprovalStatus.APPROVED)
    }

    /**
     * Checks if the borrowed book with the given ID has not been approved yet.
     *
     * **Inputs:**
     * - `id`: The ID of the `MyLibraryBorrowed` record.
     * - `p`: A parameter map (not used here).
     *
     * **Outputs:** Returns true if the borrow record's status is not APPROVED, false otherwise.
     *
     * **Implementation steps:**
     * - Retrieves the `MyLibraryBorrowed` object using its ID.
     * - Returns true if its `statusOfApproval` is not APPROVED.
     */
    private static boolean approvedSecurityClosure(Long id, Map p) {
        MyLibraryBorrowed borrowed = MyLibraryBorrowed.get(id)
        return (borrowed.statusOfApproval != ApprovalStatus.APPROVED)
    }

    /**
     * Initializes security closures and registers the library application with TaackApp.
     *
     * **Inputs:** None (called automatically after bean construction).
     *
     * **Outputs:** Registers security closures for controller methods and registers the library app with its icon.
     *
     * **Implementation steps:**
     * - Registers `securityClosure` for general controller actions such as saveAuthor, deleteAuthor, activateAuthor, etc.
     * - Registers `returnSecurityClosure` for returnBook actions to ensure only approved borrows can be returned.
     * - Registers `approvedSecurityClosure` for approveBook actions to ensure only unapproved borrows can be approved.
     * - Registers the library app with its SVG icon for TaackApp navigation.
     */
    @PostConstruct
    void init() {
        TaackUiEnablerService.securityClosure(
                this.&securityClosure,
                MyLibraryController.&createAuthor as MethodClosure,
                MyLibraryController.&deleteAuthor as MethodClosure,
                MyLibraryController.&activateAuthor as MethodClosure,
                MyLibraryController.&saveAuthor as MethodClosure,
                MyLibraryController.&selectAuthor as MethodClosure,
                MyLibraryController.&createBook as MethodClosure,
                MyLibraryController.&purchaseBook as MethodClosure,
                MyLibraryController.&purchaseAndSaveBook as MethodClosure,
                MyLibraryController.&saveBook as MethodClosure,
                MyLibraryController.&selectBookInstance as MethodClosure,
                MyLibraryController.&deleteBookInstances as MethodClosure,
                MyLibraryController.&approveBook as MethodClosure,
                MyLibraryController.&saveApprovalBookForm as MethodClosure,
                MyLibraryController.&listOfRequests as MethodClosure,
                MyLibraryController.&listOfUsers as MethodClosure,
                MyLibraryController.&showUser as MethodClosure,
        )

        TaackUiEnablerService.securityClosure(
                this.&returnSecurityClosure,
                MyLibraryController.&returnBook as MethodClosure)

        TaackUiEnablerService.securityClosure(
                this.&approvedSecurityClosure,
                MyLibraryController.&approveBook as MethodClosure)

        TaackAppRegisterService.register(new TaackApp(MyLibraryController.&index as MethodClosure, new String(this.class.getResourceAsStream("/myLibrary/library-svgrepo-com.svg").readAllBytes())))
    }


}
