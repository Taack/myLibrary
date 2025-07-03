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

// TODO 1.3.1: Define enum ApprovalStatus with PENDING, APPROVED, REJECTED.

@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBorrowed {
    // TODO 1.3.2: Define field bookInstance of type MyLibraryBookInstance.
    // TODO 1.3.3: Define field user of type User.
    // TODO 1.3.4: Define field requestDate of type Date.
    // TODO 1.3.5: Define field approvalDate of type Date.
    // TODO 1.3.6: Define field returnDate of type Date.
    // TODO 1.3.7: Define field statusOfApproval of type ApprovalStatus, default PENDING.

    // TODO 1.3.8: Define static constraints block.
    // TODO 1.3.9: In the constraints block, set approvalDate to be nullable.
    // TODO 1.3.10: In the constraints block, set returnDate to be nullable.
}
