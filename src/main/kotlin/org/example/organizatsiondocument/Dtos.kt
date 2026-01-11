package org.example.organizatsiondocument

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

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

data class OrganizationRequest(
    val name:String,
    val address:String
)

data class OrganizationResponse(
    val id:Long?,
    val name:String,
    val address:String?
)


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