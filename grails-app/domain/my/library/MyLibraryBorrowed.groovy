package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing a *Borrowing record* in the library system.
 *
 * **Purpose:** Stores borrowing details, including user, book instance, dates, and approval status.
 *
 * **To implement (TODO 1.3):**
 * - Enum:
 *   - ApprovalStatus with values: PENDING, APPROVED, REJECTED.
 * - Fields:
 *   - bookInstance (MyLibraryBookInstance): the borrowed book instance.
 *   - user (User): the user who borrowed the book.
 *   - requestDate (Date): date when borrowing was requested.
 *   - approvalDate (Date): date when borrowing was approved.
 *   - returnDate (Date): date when the book was returned.
 *   - statusOfApproval (ApprovalStatus): approval status, default PENDING.
 * - Constraints block (can remain empty or define validations as needed).
 */

enum ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBorrowed {
    MyLibraryBookInstance bookInstance
    User user
    Date requestDate
    Date approvalDate
    Date returnDate
    ApprovalStatus statusOfApproval= ApprovalStatus.PENDING

    static constraints = {}
}