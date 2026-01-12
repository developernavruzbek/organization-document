package org.example.organizatsiondocument

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
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



class UserController(){

}

@RestController
@RequestMapping("/organization")
class OrganizationController(
    private val organizationService: OrganizationService
){
    @PostMapping()
    fun create(@RequestBody organizationRequest: OrganizationRequest) = organizationService.create(organizationRequest)

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) = organizationService.getOne(id)
}

@RestController
@RequestMapping("/templates")
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
