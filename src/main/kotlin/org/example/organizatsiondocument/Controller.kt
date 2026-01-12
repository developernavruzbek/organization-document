package org.example.organizatsiondocument

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService
) {
    @PostMapping("/register")
    fun create(@RequestBody request: UserCreateRequest) = userService.create(request)

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): JwtResponse {
        return userService.loginIn(req)
    }
}


@RestController
@RequestMapping("/users")
@PreAuthorize("hasAuthority('ADMIN')")
class UserController(
    private val userService: UserService

){

    @GetMapping
    fun getAll(): List<UserResponse> {
        return userService.getAll()
    }


    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UserResponse {
        return userService.getById(id)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody body: UserUpdateRequest
    ): UserResponse {
        return userService.update(id, body)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        userService.delete(id)
        return BaseMessage(200, "User deleted successfully")
    }

}

@RestController
@RequestMapping("/organization")
@PreAuthorize("hasAuthority('ADMIN')")
class OrganizationController(
    private val organizationService: OrganizationService
){
    @PostMapping()
    fun create(@RequestBody organizationRequest: OrganizationRequest) = organizationService.create(organizationRequest)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = organizationService.getOne(id)


    @GetMapping
    fun getAll(): List<OrganizationResponse> =
        organizationService.getAll()


    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: OrganizationUpdateRequest
    ): OrganizationResponse {
        return organizationService.update(id, request)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): BaseMessage {
        organizationService.delete(id)
        return BaseMessage(200, "Organization deleted successfully")
    }

}



@RestController
@RequestMapping("/templates")
@PreAuthorize("hasAuthority('EMPLOYEE')")
class DocumentTemplateController(
    private val templateService: DocumentTemplateService
) {

    @Operation(summary = "Upload a document template with file")
    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE] // ✅ multipart qabul qilamiz
    )
    fun upload(
        @Parameter(description = "Organization ID", required = true)
        @RequestParam("organizationId") organizationId: Long,

        @Parameter(description = "Template file", required = true)
        @RequestPart("file") file: MultipartFile,

        @Parameter(description = "Template name", required = true)
        @RequestParam("templateName") templateName: String
    ): TemplateUploadResponse {
        return templateService.uploadTemplate(organizationId, file, templateName)
    }

    @Operation(summary = "Get list of template fields")
    @GetMapping("/{templateId}/fields")
    fun getTemplateFields(
        @Parameter(description = "Template ID", required = true)
        @PathVariable templateId: Long
    ): List<TemplateFieldResponse> {
        return templateService.getTemplateFields(templateId)
    }
}

@RestController
@RequestMapping("/documents")
@PreAuthorize("hasAuthority('EMPLOYEE')")
class DocumentGenerationController(
    private val documentGenerationService: DocumentGenerationService
) {

    @PostMapping("/generate")
    fun generateDocument(
        @RequestParam("templateId") templateId: Long,
        @RequestParam("userId") userId: Long,
        @RequestBody filledFieldsJson: String
    ) = documentGenerationService.generateDocument(templateId, userId, filledFieldsJson)

    @GetMapping("/user/{userId}")
    fun getUserDocuments(@PathVariable userId: Long) =
        documentGenerationService.getGeneratedDocuments(userId)
}


@RestController
@RequestMapping("/documents")
@PreAuthorize("hasAuthority('EMPLOYEE')")
class DocumentDownloadController(
    private val downloadService: DocumentDownloadService
) {

    @GetMapping("/download/{documentId}")
    fun downloadDocument(
        @PathVariable documentId: Long,
        @RequestParam(required = false) userId: Long?,
        response: HttpServletResponse
    ) {
        val file = downloadService.getGeneratedDocumentFile(documentId, userId)

        response.contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"")
        response.setContentLengthLong(file.length())

        file.inputStream().use { input ->
            response.outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}
