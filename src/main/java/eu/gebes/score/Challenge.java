package eu.gebes.score;

import foundation.icon.icx.data.Address;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

/**
 * * Modelclass for "has_my_target_accepted". So if you challenge someone, and if you want to know if he accepted or not
 */
@RequiredArgsConstructor
@Data
public class Challenge {


    @NonNull boolean accepted;
    @NonNull Address target;
    @NonNull BigInteger startTime, duration;


}
