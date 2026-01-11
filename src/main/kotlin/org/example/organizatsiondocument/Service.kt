package org.example.organizatsiondocument

import org.example.organizatsiondocument.security.JwtService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


interface UserService{
    fun create(body:UserCreateRequest)
    fun loginIn(request: LoginRequest) : JwtResponse
}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val mapper: UserMapper,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtService: JwtService,
    private val organizationRepository: OrganizationRepository
): UserService {
    @Transactional
    override fun create(body: UserCreateRequest) {

        body.run {
            userRepository.findByPhone(phone)?.let {
                throw PhoneNumberAlreadyExistsException()
            }?:run {

                val organizatsion = organizationRepository.findByIdAndDeletedFalse(organizatsionId)
                    ?:throw OrganizationNotFoundException()

                 val savedUser = userRepository.save(mapper.toEntity(body, organizatsion))
                 println("Sawed user => $savedUser")

            }
        }

    }

    override fun loginIn(request: LoginRequest): JwtResponse {
        val user =  userRepository.findByPhone(request.phone)
            ?: throw  UserNotFoundException()

        if(!passwordEncoder.matches(request.password, user.password)){
            throw PasswordIsIncorrect()
        }
        val token  = jwtService.generateToken(user.phone, user.role.name)
        return JwtResponse(token)
    }
}


interface OrganizationService{
    fun create(organizationRequest:OrganizationRequest)
    fun getOne(id:Long): OrganizationResponse
}

@Service
class OrganizationServiceImpl(
    private val organizationRepository: OrganizationRepository,
    private val mapper: OrganizationMapper

): OrganizationService {
    override fun create(organizationRequest: OrganizationRequest) {
       organizationRepository.findByName(organizationRequest.name)?.let {
           throw OrganizationNameAlreadyExists()
       }
        organizationRepository.save(mapper.toEntity(organizationRequest))
    }

    override fun getOne(id: Long): OrganizationResponse {
      organizationRepository.findByIdAndDeletedFalse(id)?.let {
          return mapper.toDto(it)

      } ?:throw OrganizationNotFoundException()

    }
}


@Service
class CustomUserDetailsService(
    private val repository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(phone: String): UserDetails {
        return repository.findByPhone(phone)?.let {
            UserDetailsResponse(
                id = it.id!!,
                myUsername = it.phone,
                fullName = it.fullName,
                role = it.role,
                myPassword = it.password,
               organization = it.organizatsion
            )
        } ?: throw UserNotFoundException()
    }
}
