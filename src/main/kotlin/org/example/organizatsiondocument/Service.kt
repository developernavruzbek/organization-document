package org.example.organizatsiondocument

import org.example.organizatsiondocument.security.JwtService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.FileInputStream
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID


interface UserService{
    fun create(body:UserCreateRequest)
    fun loginIn(request: LoginRequest) : JwtResponse

    fun getAll(): List<UserResponse>
    fun getById(id: Long): UserResponse
    fun update(id: Long, body: UserUpdateRequest): UserResponse
    fun delete(id: Long)
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

    override fun getAll(): List<UserResponse> {
        return userRepository.findAllNotDeleted()
            .map { mapper.toDto(it) }
    }

    override fun getById(id: Long): UserResponse {
        val user = userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException()

        return mapper.toDto(user)
    }


    @Transactional
    override fun update(id: Long, body: UserUpdateRequest): UserResponse {

        val user = userRepository.findByIdAndDeletedFalse(id)
            ?: throw UserNotFoundException()

        body.phone?.let { newPhone ->
            val exist = userRepository.findByPhone(newPhone)
            if (exist != null && exist.id != id) {
                throw PhoneNumberAlreadyExistsException()
            }
            if(newPhone.length>0)
                 user.phone = newPhone
        }

        body.fullName?.let {
            if (it.length>0)
            user.fullName = it }

        body.password?.let {
            if (it.length>0)
            user.password = passwordEncoder.encode(it)
        }

        body.role?.let {
            user.role = it }

        body.organizatsionId?.let { orgId ->
            if (orgId!=0L){
                val org = organizationRepository.findByIdAndDeletedFalse(orgId)
                    ?: throw OrganizationNotFoundException()
                user.organizatsion = org
            }
        }

        val updated = userRepository.save(user)
        return mapper.toDto(updated)
    }


    override fun delete(id: Long) {
           userRepository.trash(id)
            ?: throw UserNotFoundException()
    }

}


interface OrganizationService{
    fun create(organizationRequest:OrganizationRequest)
    fun getOne(id:Long): OrganizationResponse

    fun getAll(): List<OrganizationResponse>
    fun update(id: Long, request: OrganizationUpdateRequest): OrganizationResponse
    fun delete(id: Long)
}

@Service
class OrganizationServiceImpl(
    private val organizationRepository: OrganizationRepository,
    private val mapper: OrganizationMapper

): OrganizationService {
    override fun create(organizationRequest: OrganizationRequest) {
       organizationRepository.findByNameAndDeletedFalse(organizationRequest.name)?.let {
           throw OrganizationNameAlreadyExists()
       }
        organizationRepository.save(mapper.toEntity(organizationRequest))
    }

    override fun getOne(id: Long): OrganizationResponse {
      organizationRepository.findByIdAndDeletedFalse(id)?.let {
          return mapper.toDto(it)

      } ?:throw OrganizationNotFoundException()

    }

    override fun getAll(): List<OrganizationResponse> {
        return organizationRepository.findAllNotDeleted()
            .map { mapper.toDto(it) }
    }

    @Transactional
    override fun update(id: Long, request: OrganizationUpdateRequest): OrganizationResponse {
        val organization = organizationRepository.findByIdAndDeletedFalse(id)
            ?: throw OrganizationNotFoundException()

        request.name?.let { newName ->
            val exists = organizationRepository.findByNameAndDeletedFalse(newName)
            if (exists != null && exists.id != id) {
                throw OrganizationNameAlreadyExists()
            }
            if (newName.length>0)
                  organization.name = newName
        }

        if(request.address!=null && request.address.length>0)
            organization.address = request.address

        val updated = organizationRepository.save(organization)
        return mapper.toDto(updated)
    }

    override fun delete(id: Long) {
        organizationRepository.trash(id)
            ?: throw OrganizationNotFoundException()
    }
}

interface DocumentTemplateService {


    fun uploadTemplate(
        organizationId: Long,
        file: MultipartFile,
        templateName: String
    ): TemplateUploadResponse


    fun getTemplateFields(templateId: Long): List<TemplateFieldResponse>
}

@Service
class DocumentTemplateServiceImpl(
    private val templateRepository: DocumentTemplateRepository,
    private val templateFieldRepository: TemplateFieldRepository,
    private val organizationRepository: OrganizationRepository,
    private val templateMapper: DocumentTemplateMapper,
    private val fieldMapper: TemplateFieldMapper
) : DocumentTemplateService {

    private val templateBasePath = "/home/navzruzbek/IdeaProjects/work/organizatsion-document/files/templates"

    @Transactional
    override fun uploadTemplate(
        organizationId: Long,
        file: MultipartFile,
        templateName: String
    ): TemplateUploadResponse {


        val organization = organizationRepository.findByIdAndDeletedFalse(organizationId)
            ?: throw OrganizationNotFoundException()


        val extension = file.originalFilename?.substringAfterLast('.') ?: "docx"
        val fileName = "${UUID.randomUUID()}.$extension"
        val filePath = "$templateBasePath/$fileName"

        val dir = Paths.get(templateBasePath)
        if (!Files.exists(dir)) Files.createDirectories(dir)
        file.transferTo(File(filePath))


        val template = DocumentTemplate(
            templateName = templateName,
            filePath = filePath,
            organization = organization
        )
        val savedTemplate = templateRepository.save(template)

        val extractedFields = DocxFieldExtractor.extractFields(filePath)

        val templateFields = extractedFields.map { fieldKey ->
            TemplateField(
                fieldKey = fieldKey,
                label = null,
                template = savedTemplate
            )
        }
        templateFieldRepository.saveAll(templateFields)

        val fieldResponses = fieldMapper.toResponseList(templateFields)
        return templateMapper.toResponse(savedTemplate, fieldResponses)
    }

    override fun getTemplateFields(templateId: Long): List<TemplateFieldResponse> {
        val template = templateRepository.findByIdAndDeletedFalse(templateId)
            ?: throw TemplateNotFoundException()

        val fields = templateFieldRepository.findAllByTemplate(template)
        return fieldMapper.toResponseList(fields)
    }
}



interface DocumentGenerationService {
    fun generateDocument(templateId: Long, userId: Long, filledFieldsJson: String): GeneratedDocumentResponse
    fun getGeneratedDocuments(userId: Long): List<GeneratedDocumentResponse>
}

@Service
class DocumentGenerationServiceImpl(
    private val templateRepository: DocumentTemplateRepository,
    private val generatedDocumentRepository: GeneratedDocumentRepository,
    private val userRepository: UserRepository
) : DocumentGenerationService {

    private val generatedBasePath = "/home/navzruzbek/IdeaProjects/work/organizatsion-document/files/generated"

    @Transactional
    override fun generateDocument(
        templateId: Long,
        userId: Long,
        filledFieldsJson: String
    ): GeneratedDocumentResponse {
        // --- Template va userni olish ---
        val template = templateRepository.findByIdAndDeletedFalse(templateId)
            ?: throw TemplateNotFoundException()

        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException()

        // --- Folder mavjudligini tekshirish ---
        val dir = Paths.get(generatedBasePath)
        if (!Files.exists(dir)) Files.createDirectories(dir)

        // --- DOCX faylni ochish ---
        val doc = XWPFDocument(FileInputStream(template.filePath))

        // --- JSON → Map<String, String> ---
        val filledMap: Map<String, String> = jacksonObjectMapper().readValue(filledFieldsJson)

        // --- Helper funksiya: paragraph matnini to‘liq almashtirish ---
        fun replaceParagraphText(paragraph: XWPFParagraph) {
            var text = paragraph.text
            filledMap.forEach { (key, value) ->
                text = text.replace("{{${key}}}", value)
            }
            // Runlarni tozalash
            for (i in paragraph.runs.size - 1 downTo 0) {
                paragraph.removeRun(i)
            }
            // Yangi run yaratish va matn qo‘yish
            paragraph.createRun().setText(text)
        }

        // --- Barcha paragraphlarni to‘ldirish ---
        doc.paragraphs.forEach { replaceParagraphText(it) }

        // --- Table ichidagi paragraphlarni ham to‘ldirish ---
        doc.tables.forEach { table ->
            table.rows.forEach { row ->
                row.tableCells.forEach { cell ->
                    cell.paragraphs.forEach { replaceParagraphText(it) }
                }
            }
        }

        // --- Faylni saqlash ---
        val outputFileName = "${UUID.randomUUID()}.docx"
        val outputPath = "$generatedBasePath/$outputFileName"
        doc.write(File(outputPath).outputStream())
        doc.close()

        // --- Entity saqlash ---
        val generated = GeneratedDocument(
            template = template,
            user = user,
            filledFieldsJson = filledFieldsJson,
            filePath = outputPath
        )
        val saved = generatedDocumentRepository.save(generated)

        // --- Javobni qaytarish ---
        return GeneratedDocumentResponse(
            id = saved.id!!,
            templateName = template.templateName,
            userFullName = user.fullName,
            filePath = outputPath,
            createdDate = saved.createdDate.toString()
        )
    }

    @Transactional(readOnly = true)
    override fun getGeneratedDocuments(userId: Long): List<GeneratedDocumentResponse> {
        val user = userRepository.findByIdAndDeletedFalse(userId)
            ?: throw UserNotFoundException()

        return generatedDocumentRepository.findAllByUser(user)
            .map { doc ->
                GeneratedDocumentResponse(
                    id = doc.id!!,
                    templateName = doc.template.templateName,
                    userFullName = doc.user.fullName,
                    filePath = doc.filePath,
                    createdDate = doc.createdDate.toString()
                )
            }
    }
}


interface DocumentDownloadService {
    fun getGeneratedDocumentFile(documentId: Long, userId: Long? = null): File
}

@Service
class DocumentDownloadServiceImpl(
    private val generatedDocumentRepository: GeneratedDocumentRepository
) : DocumentDownloadService {

    override fun getGeneratedDocumentFile(documentId: Long, userId: Long?): File {
        val generated = generatedDocumentRepository.findById(documentId)
            .orElseThrow { RuntimeException("Generated document not found") }

        // Agar userId berilgan bo‘lsa, tekshirish
        if (userId != null && generated.user.id != userId) {
            throw RuntimeException("You are not allowed to download this document")
        }

        val file = File(generated.filePath)
        if (!file.exists()) {
            throw RuntimeException("File not found on server")
        }

        return file
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
