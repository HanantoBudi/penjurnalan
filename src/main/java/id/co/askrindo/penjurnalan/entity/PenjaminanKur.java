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
@Table(name = "penjaminan_kur", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PenjaminanKur {
    @Id
    @Column(name = "id", columnDefinition="uniqueidentifier")
    private String id;

    @Column(name = "kode_bank")
    private String kodeBank;

    @Column(name = "kode_uker")
    private String kodeUker;

    @Column(name = "nama_uker")
    private String namaUker;

    @Column(name = "no_sertifikat")
    private String noSertifikat;

    @Column(name = "no_pk")
    private String noPk;

    @Column(name = "tanggal_pk")
    private Date tanggalPk;

    @Column(name = "tanggal_awal")
    private Date tanggalAwal;

    @Column(name = "tanggal_akhir")
    private Date tanggalAkhir;

    @Column(name = "periode")
    private String periode;

    @Column(name = "jenis_kredit")
    private String jenisKredit;

    @Column(name = "jenis_kur")
    private String jenisKur;

    @Column(name = "nama_debitur")
    private String namaDebitur;

    @Column(name = "alamat_debitur")
    private String alamat_debitur;

    @Column(name = "kode_pos")
    private String kodePos;

    @Column(name = "tanggal_lahir")
    private Date tanggalLahir;

    @Column(name = "jenis_kelamin")
    private String jenisKelamin;

    @Column(name = "no_telepon")
    private String noTelepon;

    @Column(name = "no_hp")
    private String noHp;

    @Column(name = "jenis_identitas")
    private String jenisIdentitas;

    @Column(name = "no_identitas")
    private String noIdentitas;

    @Column(name = "npwp")
    private String npwp;

    @Column(name = "no_ijin_usaha")
    private String noIjinUsaha;

    @Column(name = "jenis_pekerjaan")
    private String jenisPekerjaan;

    @Column(name = "pekerjaan")
    private String pekerjaan;

    @Column(name = "plafon_kredit")
    private BigDecimal plafonKredit;

    @Column(name = "jangka_waktu")
    private Integer jangkaWaktu;

    @Column(name = "kode_sektor")
    private String kodeSektor;

    @Column(name = "coverage")
    private Integer coverage;

    @Column(name = "jml_t_kerja")
    private Integer jmlTKerja;

    @Column(name = "kode_cabang_ask")
    private String kodeCabangAsk;

    @Column(name = "nama_cabang_ask")
    private String namaCabangAsk;

    @Column(name = "tanggal_rekam")
    private Date tanggalRekam;

    @Column(name = "jenis_linkage")
    private String jenisLinkage;

    @Column(name = "lembaga_linkage")
    private String lembagaLinkage;

    @Column(name = "status_lunas")
    private String statusLunas;

    @Column(name = "status_kolektibilitas")
    private String statusKolektibilitas;

    @Column(name = "usaha_produktif")
    private String usahaProduktif;

    @Column(name = "status_aplikasi")
    private String statusAplikasi;

    @Column(name = "modal_usaha")
    private BigDecimal modalUsaha;

    @Column(name = "alamat_usaha")
    private String alamatUsaha;

    @Column(name = "tanggal_mulai_usaha")
    private Date tanggalMulaiUsaha;

    @Column(name = "omset")
    private BigDecimal omset;

    @Column(name = "tgl_approve_spv")
    private Date tglApproveSpv;

    @Column(name = "status_proses")
    private String statusProses;

    @Column(name = "flag_terbit_acs")
    private String flagTerbitAcs;

    @Column(name = "flag_rehit")
    private String flagRehit;

    @Column(name = "loan_type")
    private String loanType;

    @Column(name = "lama_usaha")
    private Integer lamaUsaha;

    @Column(name = "tgl_sertifikat")
    private Date tglSertifikat;

    @Column(name = "tgl_rekam_sertifikat")
    private Date tglRekamSertifikat;

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

    @Column(name = "keterangan_tolak")
    private String keteranganTolak;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "request_id_org")
    private String requestIdOrg;

    @Column(name = "counter_rehit")
    private Integer counterRehit;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "no_sertifikat_lama")
    private String noSertifikatLama;

    @Column(name = "ket_audit_trail")
    private String ketAuditTrail;

    @Column(name = "flag_sikp")
    private String flag_sikp;

    @Column(name = "flag_pembatalan")
    private String flagPembatalan;

    @Column(name = "flag_os_penjaminan")
    private Integer flagOsPenjaminan;
}
