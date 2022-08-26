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
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "klaim_kur", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class KlaimKur {
    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "kode_bank")
    private String kodeBank;

    @Column(name = "kode_uker")
    private String kodeUker;

    @Column(name = "no_rekening")
    private String noRekening;

    @Column(name = "no_sertifikat")
    private String noSertifikat;

    @Column(name = "baki_debet")
    private BigDecimal bakiDebet;

    @Column(name = "nilai_tuntutan")
    private BigDecimal nilaiTuntutan;

    @Column(name = "nilai_kerugian")
    private BigDecimal nilaiKerugian;

    @Column(name = "bunga")
    private BigDecimal bunga;

    @Column(name = "denda")
    private BigDecimal denda;

    @Column(name = "no_stgr")
    private String noStgr;

    @Column(name = "tgl_stgr")
    private Date tglStgr;

    @Column(name = "nilai_taksasi")
    private BigDecimal nilaiTaksasi;

    @Column(name = "status_kolektibilitas")
    private String statusKolektibilitas;

    @Column(name = "tipe_claim")
    private String tipeClaim;

    @Column(name = "track_status")
    private String trackStatus;

    @Column(name = "status_proses_acs")
    private String statusProsesAcs;

    @Column(name = "kode_penyebab_klaim")
    private String kodePenyebabKlaim;

    @Column(name = "keterangan_penyebab_klaim")
    private String keteranganPenyebabKlaim;

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

    @Column(name = "response_code_acs")
    private String responseCodeAcs;

    @Column(name = "response_message")
    private String responseMessage;

    @Column(name = "ket_audit_trail")
    private String ketAuditTrail;

    @Column(name = "status_claim")
    private String statusClaim;

    @Column(name = "status_inquiry_claim")
    private String statusInquiryClaim;

    @Column(name = "response_message_inquiry")
    private String responseMessageInquiry;

    @Column(name = "claim_type")
    private String claimType;

    @Column(name = "keterangan_banding")
    private String keteranganBanding;

    @Column(name = "net_claim_approved")
    private BigDecimal netClaimApproved;

    @Column(name = "nilai_tuntutan_before")
    private BigDecimal nilaiTuntutanBefore;

    @Column(name = "jenis_kredit")
    private String jenisKredit;

    @Column(name = "jenis_kur")
    private String jenisKur;

    @Column(name = "no_klaim")
    private String noKlaim;

    @Column(name = "cabang_rekanan")
    private String cabangRekanan;

    @Column(name = "flag_tamdat")
    private String flagTamdat;

    @Column(name = "acs_registration_id")
    private String acsRegistrationId;

    @Column(name = "date_of_loss")
    private Date dateOfLoss;

    @Column(name = "cover_letter_date")
    private Date coverLetterDate;

    @Column(name = "status_pembayaran")
    private String statusPembayaran;

    @Column(name = "selisih_net_claim")
    private BigDecimal selisihNetClaim;

    @Column(name = "no_jurnal", length = 50)
    private String noJurnal;

    @Column(name = "tanggal_posting")
    private Date tanggalPosting;
}