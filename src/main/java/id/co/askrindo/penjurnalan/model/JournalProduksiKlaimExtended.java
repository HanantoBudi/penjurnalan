package id.co.askrindo.penjurnalan.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class JournalProduksiKlaimExtended {
    private String id;
    private String journalID;
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
    private String dataID;
    private String sppaNo;
    private Long dataSource;
}
