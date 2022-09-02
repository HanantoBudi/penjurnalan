package id.co.askrindo.penjurnalan.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JournalProduksiIJPExtended {
    private String id;
    private String journalId;
    private String kodeCob;
    private String namaCob;
    private String nomorPolis;
    private String tanggalTerbitPolis;
    private String kodeBusinessPartner;
    private String namaBusinessPartner;
    private String kodeTertanggung;
    private String namaTertanggung;
    private String reinsuranceFacultativeSlip;
    private String periodeAwal;
    private String periodeAkhir;
    private String currencyCode;
    private Long sumInsured;
    private String dueDate;
    private Double grossPremium;
    private Long discount;
    private Long policyFee;
    private Long stampDuty;
    private Long commission;
    private Long ppn;
    private Long pph;
    private String creatorIPAddress;
    private String modifiedByIPAddress;
    private String dataId;
    private String sppaNo;
    private Long dataSource;
}
