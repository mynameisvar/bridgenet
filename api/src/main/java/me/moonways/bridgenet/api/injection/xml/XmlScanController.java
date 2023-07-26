package me.moonways.bridgenet.api.injection.xml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.*;

@Getter
@ToString
@XmlRootElement(name = "scanner")
@XmlType(propOrder = {"annotationClass", "targetClass"})
public class XmlScanController {

    @Setter(onMethod_ = @XmlElement)
    private String annotationClass;

    @Setter(onMethod_ = @XmlElement)
    private String targetClass;
}