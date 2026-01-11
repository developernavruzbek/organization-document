package org.example.organizatsiondocument

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserMapper(
    private val passwordEncoder:PasswordEncoder
){
    fun toEntity(userRequest: UserCreateRequest,organizatsion: Organizatsion): User{
        userRequest.run {
            return User(
                fullName = fullName,
                phone = phone,
                password = passwordEncoder.encode(password),
                role = role,
                organizatsion = organizatsion,
            )
        }
    }

    /*
    fun toDto(user: User): UserResponse{
        user.run {
            return UserResponse(
                id = id,
                firstName = firstName,
                role = role,
                status = status
            )
        }
    }

    fun toDtoFull(user: User): UserFullResponse{
        user.run {
            return UserFullResponse(
                id = id,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                role  = role,
                wareHouseId = wareHouse.id,
                wareHouseName = wareHouse.name,
                status = status
            )
        }
    }

     */


}

@Component
class OrganizationMapper(

){
    fun toEntity(organizationRequest: OrganizationRequest): Organizatsion{
        organizationRequest.run {
            return Organizatsion(
                name  = name,
                address = address
            )
        }
    }

    fun toDto(organizatsion: Organizatsion): OrganizationResponse{
        organizatsion.run {
            return OrganizationResponse(
                id = id,
                name = name,
                address = address
            )
        }
    }
}