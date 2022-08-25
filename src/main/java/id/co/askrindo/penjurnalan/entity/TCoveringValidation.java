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
@Table(name = "t_covering_validation", schema = "brisurf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TCoveringValidation {

    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "remark", length = 40)
    private String remark;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Date modifiedDate;

    @Column(name = "created_by", length = 50)
    @CreatedBy
    private String createdBy;

    @Column(name = "modified_by")
    @LastModifiedBy
    private String modifiedBy;

}
