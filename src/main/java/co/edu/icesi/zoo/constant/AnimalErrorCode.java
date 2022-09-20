package co.edu.icesi.zoo.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AnimalErrorCode {

    CODE_01("CODE_01"),
    CODE_02("CODE_02"),
    CODE_03("CODE_03"),
    CODE_04("CODE_04");

    private String code;
}
