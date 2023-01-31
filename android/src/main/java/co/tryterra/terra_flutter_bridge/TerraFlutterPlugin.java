package co.tryterra.terra_flutter_bridge;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;
import java.util.Date;

import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;

import co.tryterra.terra.enums.DataTypes;
import co.tryterra.terra.models.Update;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;

import com.google.gson.Gson;

import co.tryterra.terra.enums.Connections;
import co.tryterra.terra.enums.CustomPermissions;
import co.tryterra.terra.Terra;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * TerraFlutterPlugin
 */
public class TerraFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;
    private FlutterActivity activity = null;
    private BinaryMessenger binaryMessenger = null;

    private Gson gson = new Gson();

    public Terra terra;

    // parsing
    private Connections parseConnection(String connection) {
        switch (connection) {
            case "SAMSUNG":
                return Connections.SAMSUNG;
            case "GOOGLE":
                return Connections.GOOGLE_FIT;
            case "FREESTYLE_LIBRE":
                return Connections.FREESTYLE_LIBRE;
        }
        return null;
    }

    private CustomPermissions parseCustomPermission(String customPermission) {
        switch (customPermission) {
            case "WORKOUT_TYPES":
                return CustomPermissions.WORKOUT_TYPE;
            case "ACTIVITY_SUMMARY":
                return CustomPermissions.ACTIVITY_SUMMARY;
            case "LOCATION":
                return CustomPermissions.LOCATION;
            case "CALORIES":
                return CustomPermissions.CALORIES;
            case "STEPS":
                return CustomPermissions.STEPS;
            case "HEART_RATE":
                return CustomPermissions.HEART_RATE;
            case "HEART_RATE_VARIABILITY":
                return CustomPermissions.HEART_RATE_VARIABILITY;
            case "VO2MAX":
                return CustomPermissions.VO2MAX;
            case "HEIGHT":
                return CustomPermissions.HEIGHT;
            case "ACTIVE_DURATIONS":
                return CustomPermissions.ACTIVE_DURATIONS;
            case "WEIGHT":
                return CustomPermissions.WEIGHT;
            case "FLIGHTS_CLIMBED":
                return CustomPermissions.FLIGHTS_CLIMBED;
            case "BMI":
                return CustomPermissions.BMI;
            case "BODY_FAT":
                return CustomPermissions.BODY_FAT;
            case "EXERCISE_DISTANCE":
                return CustomPermissions.EXERCISE_DISTANCE;
            case "GENDER":
                return CustomPermissions.GENDER;
            case "DATE_OF_BIRTH":
                return CustomPermissions.DATE_OF_BIRTH;
            case "BASAL_ENERGY_BURNED":
                return CustomPermissions.BASAL_ENERGY_BURNED;
            case "SWIMMING_SUMMARY":
                return CustomPermissions.SWIMMING_SUMMARY;
            case "RESTING_HEART_RATE":
                return CustomPermissions.RESTING_HEART_RATE;
            case "BLOOD_PRESSURE":
                return CustomPermissions.BLOOD_PRESSURE;
            case "BLOOD_GLUCOSE":
                return CustomPermissions.BLOOD_GLUCOSE;
            case "BODY_TEMPERATURE":
                return CustomPermissions.BODY_TEMPERATURE;
            case "MINDFULNESS":
                return CustomPermissions.MINDFULNESS;
            case "LEAN_BODY_MASS":
                return CustomPermissions.LEAN_BODY_MASS;
            case "OXYGEN_SATURATION":
                return CustomPermissions.OXYGEN_SATURATION;
            case "SLEEP_ANALYSIS":
                return CustomPermissions.SLEEP_ANALYSIS;
            case "RESPIRATORY_RATE":
                return CustomPermissions.RESPIRATORY_RATE;
            case "NUTRITION_SODIUM":
                return CustomPermissions.NUTRITION_SODIUM;
            case "NUTRITION_PROTEIN":
                return CustomPermissions.NUTRITION_PROTEIN;
            case "NUTRITION_CARBOHYDRATES":
                return CustomPermissions.NUTRITION_CARBOHYDRATES;
            case "NUTRITION_FIBRE":
                return CustomPermissions.NUTRITION_FIBRE;
            case "NUTRITION_FAT_TOTAL":
                return CustomPermissions.NUTRITION_FAT_TOTAL;
            case "NUTRITION_SUGAR":
                return CustomPermissions.NUTRITION_SUGAR;
            case "NUTRITION_VITAMIN_C":
                return CustomPermissions.NUTRITION_VITAMIN_C;
            case "NUTRITION_VITAMIN_A":
                return CustomPermissions.NUTRITION_VITAMIN_A;
            case "NUTRITION_CALORIES":
                return CustomPermissions.NUTRITION_CALORIES;
            case "NUTRITION_WATER":
                return CustomPermissions.NUTRITION_WATER;
            case "NUTRITION_CHOLESTEROL":
                return CustomPermissions.NUTRITION_CHOLESTEROL;
            default:
                return null;
        }
    }

    private void testFunction(String text, @NonNull Result result) {
        result.success(text);
    }


    // init
    private void initTerra(
            String devID,
            String referenceId,
            Result result
    ) {
        try {
            this.terra = new Terra(
                    devID,
                    Objects.requireNonNull(this.context),
                    referenceId,
                    (success) -> {
                        result.success(success);
                        return Unit.INSTANCE;
                    }
            );
        } catch (Exception e) {
            result.error("Init Failure", "Could not initialise Terra", e);
            result.success(false);
        }
    }

    private void initConnection(
            String connection,
            String token,
            Boolean schedulerOn,
            ArrayList<String> customPermissions,
            Result result
    ) {
        if (parseConnection(connection) == null) {
            result.error("Connection Failure", "Invalid Connection has been passed for the android platform", null);
            return;
        }

        HashSet<CustomPermissions> cPermissions = new HashSet<>();
        for (Object customPermission : customPermissions) {
            if (customPermission == null) {
                continue;
            }
            CustomPermissions cPermission = parseCustomPermission((String) customPermission);
            if (cPermission == null) {
                continue;
            }
            cPermissions.add(cPermission);
        }

        this.terra.initConnection(
                Objects.requireNonNull(parseConnection(connection)),
                token, Objects.requireNonNull(this.context),
                cPermissions,
                schedulerOn,
                null,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                });
    }

    private void getUserId(String connection, Result result) {
        try {
            result.success(terra.getUserId(
                    Objects.requireNonNull(parseConnection(connection))
            ));
        } catch (Exception e) {
            result.error("Getter Failure", "Could not get user id", e);
        }
    }

    // getters
    private void getAthlete(String connection, Result result) {
        this.terra.getAthlete(
                Objects.requireNonNull(parseConnection(connection)),
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    private void getActivity(String connection, Date startDate, Date endDate, Result result) {
        this.terra.getActivity(
                Objects.requireNonNull(parseConnection(connection)),
                startDate,
                endDate,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    private void getBody(String connection, Date startDate, Date endDate, Result result) {
        this.terra.getBody(
                Objects.requireNonNull(parseConnection(connection)),
                startDate,
                endDate,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    private void getDaily(String connection, Date startDate, Date endDate, Result result) {
        this.terra.getDaily(
                Objects.requireNonNull(parseConnection(connection)),
                startDate,
                endDate,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    private void getNutrition(String connection, Date startDate, Date endDate, Result result) {
        this.terra.getNutrition(
                Objects.requireNonNull(parseConnection(connection)),
                startDate,
                endDate,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    private void getSleep(String connection, Date startDate, Date endDate, Result result) {
        this.terra.getSleep(
                Objects.requireNonNull(parseConnection(connection)),
                startDate,
                endDate,
                (success) -> {
                    result.success(success);
                    return Unit.INSTANCE;
                }
        );
    }

    // freestyle
    private void readGlucoseData(Result result) {
        try {
            this.terra.readGlucoseData((details) -> {
                result.success(gson.toJson(details));
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            result.error("Sensor Activation Failure", "Could not activate freestyle sensor", e);
        }
    }

    // Extra
    private void updateHandler(Result result) {
        Terra.Companion.setUpdateHandler((dataTypes, update) -> {
            Log.d("updateHandler", update.component2().toString());
            return Unit.INSTANCE;
        });
        result.success("");
    }

    private void disconnect(String a, String b, String c, Result result){
        this.terra.revokePermissions(Connections.SAMSUNG);
        Terra.Companion.disconnect(a,b,c);
        result.success(true);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        binaryMessenger = flutterPluginBinding.getBinaryMessenger();
        // this.context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        this.context = (Context) binding.getActivity();
        channel = new MethodChannel(binaryMessenger, "terra_flutter_bridge");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "testFunction":
                testFunction(call.argument("text"), result);
                break;
            case "initTerra":
                initTerra(
                        call.argument("devID"),
                        call.argument("referenceID"),
                        result
                );
                break;
            case "initConnection":
                initConnection(
                        call.argument("connection"),
                        call.argument("token"),
                        call.argument("schedulerOn"),
                        call.argument("customPermissions"),
                        result
                );
                break;
            case "getActivity":
                getActivity(
                        call.argument("connection"),
                        Date.from(Instant.parse(call.argument("startDate"))),
                        Date.from(Instant.parse(call.argument("endDate"))),
                        result
                );
                break;
            case "getBody":
                getBody(
                        call.argument("connection"),
                        Date.from(Instant.parse(call.argument("startDate"))),
                        Date.from(Instant.parse(call.argument("endDate"))),
                        result
                );
                break;
            case "getDaily":
                getDaily(
                        call.argument("connection"),
                        Date.from(Instant.parse(call.argument("startDate"))),
                        Date.from(Instant.parse(call.argument("endDate"))),
                        result
                );
                break;
            case "getNutrition":
                getNutrition(
                        call.argument("connection"),
                        Date.from(Instant.parse(call.argument("startDate"))),
                        Date.from(Instant.parse(call.argument("endDate"))),
                        result
                );
                break;
            case "getSleep":
                getSleep(
                        call.argument("connection"),
                        Date.from(Instant.parse(call.argument("startDate"))),
                        Date.from(Instant.parse(call.argument("endDate"))),
                        result
                );
                break;
            case "getAthlete":
                getAthlete(
                        call.argument("connection"),
                        result
                );
                break;
            case "readGlucoseData":
                readGlucoseData(
                        result
                );
                break;
            case "getUserId":
                getUserId(
                        call.argument("connection"),
                        result
                );
                break;
            case "updateHandler":
                updateHandler(result);
                break;
            case "disconnect":
                disconnect(
                        call.argument("user_id"),
                        call.argument("dev_id"),
                        call.argument("api_key"),
                        result
                );
            default:
                result.notImplemented();
                break;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

}
