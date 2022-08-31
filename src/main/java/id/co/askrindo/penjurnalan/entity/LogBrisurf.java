package id.co.askrindo.penjurnalan.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "log_brisurf", schema = "brisurf")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class LogBrisurf {
    @Id
    @GenericGenerator(name = "generator", strategy = "guid", parameters = {})
    @GeneratedValue(generator = "generator")
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "no_rekening", length = 50)
    private String noRekening;

    @Column(name = "service", length = 100)
    private String service;

    @Column(name = "json_request")
    @Nationalized
    private String jsonRequest;

    @Column(name = "json_response")
    @Nationalized
    private String jsonResponse;

    @Column(name = "response_code")
    @Nationalized
    private String responseCode;

    @Column(name = "response_desc")
    @Nationalized
    private String responseDesc;

    @Column(name = "is_incoming_request")
    private Boolean isIncomingRequest;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "created_date")
    @CreatedDate
    private Date createdDate;

    @Column(name = "modified_date")
    @LastModifiedDate
    private Date modifiedDate;
}