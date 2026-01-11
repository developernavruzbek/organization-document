package org.example.organizatsiondocument

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
)
{

    @PostMapping("/register")
    fun create(@RequestBody request: UserCreateRequest) = userService.create(request)


    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): JwtResponse {
        return userService.loginIn(req)
    }

}


@RestController
@RequestMapping("/organization")
class OrganizationController(
    private val organizationService: OrganizationService
){

    @PostMapping()
    fun create(@RequestBody organizationRequest: OrganizationRequest) = organizationService.create(organizationRequest)

       @GetMapping("/{id}")
    fun getOne(@PathVariable id:Long) = organizationService.getOne(id)
}