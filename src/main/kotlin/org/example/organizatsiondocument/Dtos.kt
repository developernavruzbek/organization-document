package org.example.organizatsiondocument

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.io.FileInputStream

data class BaseMessage(
    val code: Long? = null,
    val message: String? = null
)

data class UserCreateRequest(
    val fullName: String,
    val phone:String,
    val password:String,
    val organizatsionId: Long,
    val role: UserRole,
)

data class UserUpdateRequest(
    val fullName: String?,
    val phone: String?,
    val password: String?,
    val role: UserRole?,
    val organizatsionId: Long?
)

data class UserResponse(
    val id: Long,
    val fullName: String,
    val phone: String,
    val role: UserRole,
    val organizationId: Long,
    val organizationName: String?
)


data class OrganizationRequest(
    val name:String,
    val address:String
)

data class OrganizationResponse(
    val id:Long?,
    val name:String,
    val address:String?
)

data class OrganizationUpdateRequest(
    val name: String?,
    val address: String?
)


data class TemplateUploadRequest(
    val organizationId: Long
)

data class TemplateUploadResponse(
    val templateId: Long,
    val templateName: String,
    val organizationId: Long,
    val fields: List<TemplateFieldResponse>
)

data class TemplateFieldResponse(
    val id: Long?,
    val key: String,
    val label: String?
)

data class GeneratedDocumentResponse(
    val id: Long,
    val templateName: String,
    val userFullName: String,
    val filePath: String,
    val createdDate: String
)


object DocxFieldExtractor {

    /**
     * Fayldan {{field}} larni extract qiladi
     */
    fun extractFields(filePath: String): List<String> {
        val fis = FileInputStream(filePath)
        val doc = XWPFDocument(fis)
        val fields = mutableSetOf<String>()

        // Paragraphlarni tekshirish
        for (paragraph in doc.paragraphs) {
            val regex = "\\{\\{(.*?)}}".toRegex()
            regex.findAll(paragraph.text).forEach { match ->
                fields.add(match.groupValues[1])
            }
        }

        // Table ichidagi matnlarni ham tekshirish
        for (table in doc.tables) {
            for (row in table.rows) {
                for (cell in row.tableCells) {
                    val regex = "\\{\\{(.*?)}}".toRegex()
                    regex.findAll(cell.text).forEach { match ->
                        fields.add(match.groupValues[1])
                    }
                }
            }
        }

        fis.close()
        return fields.toList()
    }
}




data class UserDetailsResponse(
    val id: Long,
    val myUsername: String,
    val fullName: String?,
    val role: UserRole,
    val myPassword: String,
    val organization: Organizatsion
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String {
        return myPassword
    }

    override fun getUsername(): String {
        return myUsername
    }
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true


}

data class LoginRequest(val phone: String, val password: String)
data class JwtResponse(val token: String)