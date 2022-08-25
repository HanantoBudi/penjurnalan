package id.co.askrindo.penjurnalan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "m_product_acs", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductAcs {
    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "jenis_kredit")
    private String jenisKredit;

    @Column(name = "jenisKur")
    private String jenisKur;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Date modifiedDate;

    @Column(name = "created_by", length = 50)
    @CreatedBy
    private String createdBy;

    @Column(name = "modified_by", length = 50)
    @LastModifiedBy
    private String modifiedBy;
}
