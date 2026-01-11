package org.example.organizatsiondocument

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import jakarta.persistence.Temporal
import jakarta.persistence.TemporalType
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: Long? = null,
    @LastModifiedBy var lastModifiedBy: Long? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false // (true - o'chirilgan bo'lsa)  (false - o'chirilmagan)
)

@Entity
class Organizatsion(
    @Column(nullable = false, unique = true) val name :String,
    var address:String? =null
): BaseEntity()

@Entity
@Table(name  = "users")
class User(
    var fullName:String,
    @Column(nullable = false, unique = true) var phone:String,
    var password:String,
    @ManyToOne var organizatsion: Organizatsion,
    @Enumerated(value = EnumType.STRING) var role: UserRole = UserRole.EMPLOYEE
): BaseEntity()


@Entity
class DocumentTemplate(
    @Column(nullable = false)
    var templateName: String,

    @Column(nullable = false)
    var filePath: String,

    @ManyToOne
    var organization: Organizatsion
) : BaseEntity()


@Entity
@Table(name = "template_fields")
class TemplateField(
    @Column(nullable = false)
    var fieldKey: String,
    var label: String? = null,
    @ManyToOne
    var template: DocumentTemplate
) : BaseEntity()


@Entity
class GeneratedDocument(
    @ManyToOne
    var template: DocumentTemplate,

    @ManyToOne
    var user: User,

    @Column(columnDefinition = "TEXT")
    var filledFieldsJson: String,

    @Column(nullable = false)
    var filePath: String
) : BaseEntity()
