package gm.rahmanproperties.optibank.dtos;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FraisBancaireDTo {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty compteId = new SimpleStringProperty();
    private final StringProperty type = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> montant = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateApplication = new SimpleObjectProperty<>();

    public FraisBancaireDTo() {}

    // Getters et setters avec propriétés JavaFX
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }
    public void setId(String id) { this.id.set(id); }

    public String getCompteId() { return compteId.get(); }
    public StringProperty compteIdProperty() { return compteId; }
    public void setCompteId(String compteId) { this.compteId.set(compteId); }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }
    public void setType(String type) { this.type.set(type); }

    public BigDecimal getMontant() { return montant.get(); }
    public ObjectProperty<BigDecimal> montantProperty() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant.set(montant); }

    public LocalDateTime getDateApplication() { return dateApplication.get(); }
    public ObjectProperty<LocalDateTime> dateApplicationProperty() { return dateApplication; }
    public void setDateApplication(LocalDateTime date) { this.dateApplication.set(date); }
}
