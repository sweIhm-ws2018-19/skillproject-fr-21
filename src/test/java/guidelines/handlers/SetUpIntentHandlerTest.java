package guidelines.handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.*;
import com.amazon.ask.model.slu.entityresolution.*;
import com.amazon.ask.response.ResponseBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import guidelines.OutputStrings;
import guidelines.StatusAttributes;
import guidelines.model.Address;
import guidelines.model.AddressResolver;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class SetUpIntentHandlerTest {

    private final String[] slotsNameList = {"YesNoSlot_Location","Homeaddress",
            "NameHome","DestinationA","NameA",
            "YesNoSlot_wantSecondDest","DestinationB","NameB",
            "YesNoSlot_wantThirdDest","DestinationC","NameC"};

    private final String[] profileValues = {"Untertaxetweg 150 Gauting", "Zuhause",
            "Lothstraße 64 München", "Uni",
            "Landsbergerstraße 184 München", "Arbeit",
            "Flughafen München", "Flughafen"};

    private final String[] testAddresses = {
            //man kann hier gerne noch weitere Testadressen zum Array hinzufügen, die werden dann alle automatisch getestet
            "Untertaxetweg 150",
            "Untertaxetweg hundertfünfzig",
            "Hauptplatz zwei gauting",
            //"Leierkasten München", //TODO geht noch nicht! -> Benni
            "Flughafen München",
            "Olympiapark München",
            "Marienplatz",
            "Oktoberfest",
            "Apple Store München",
            //"Hofbräuhaus München", //TODO der geht auch nicht! -> Benni
            //"Landsberger Straße hundertvierundachzig", //TODO der auch nicht
            "Lothstraße 64"
    };


    private SetUpIntentHandler handler;



    /**
     *
     * @param slotValues die Werte die in den Slots gespeichert werden sollen.
     *                   Kann auch "null" sein, wenn man nur leere Slots will.
     * @return die fertig gebauten slots
     */
    private Slot[] getSlots(List<String> slotValues){
        Slot[] slots = new Slot[12];
        for(int i = 0; i< 11; i++){
            if(slotValues != null && slotValues.size() > i)
                slots[i] = mockSlotWithValue(slotsNameList[i], slotValues.get(i),SlotConfirmationStatus.NONE);
            else
                slots[i] = mockSlotWithValue(slotsNameList[i], null,SlotConfirmationStatus.NONE);
        }


        return slots;
    }





    /**
     *
     * @param processValue der Wert, der den Fortschritt der Einrichtung angibt (siehe StatusAttributes)
     * @param slotValues die Liste mit den Werten, die in die Slots gehören
     * @return liefert den Input, den der SetUpIntentHandler im Laufe der Einrichtung erhält
     */
    private HandlerInput mockInputSetUpInProcess(StatusAttributes processValue, List<String> slotValues){
        Slot[] slots = getSlots(slotValues);
        Map<String, Object> sessionList = new HashMap<>();
        sessionList.put(StatusAttributes.KEY_PROCESS.toString(),processValue.toString());
        AttributesManager attributesManager = mockAttributesManager(sessionList, null);
        HandlerInput mockInput = Mockito.mock(HandlerInput.class);
        when(mockInput.getRequestEnvelope()).thenReturn(
                RequestEnvelope.builder()
                                .withRequest(
                                        mockRequest("SetUpIntent", slots, DialogState.IN_PROGRESS))
                                .build());
        when(mockInput.getAttributesManager()).thenReturn(attributesManager);
        when(mockInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        return mockInput;

    }


    /**
     * liefert einen Slot zurück
     * @param slotName der Name des Slots
     * @param slotValue der Value des Slots
     * @return
     */
    private Slot mockSlotWithValue(String slotName,String slotValue, SlotConfirmationStatus confirmationStatus){
        return Slot.builder()
                .withName(slotName)
                .withValue(slotValue)
                .withConfirmationStatus(confirmationStatus)
                .withResolutions(Resolutions.builder()
                        .addResolutionsPerAuthorityItem(Resolution.builder()
                                .addValuesItem(ValueWrapper.builder()
                                        .withValue(Value.builder()
                                                .withName(slotValue).build())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    /**
     *
     * @param setUpProcessValue der Wert, der den Fortschritt der Einrichtung angibt (siehe StatusAttributes)
     * @return liefert ein Session Objekt mit dem richtigen Session Attribut zurück,
     *      das den Fortschritt der Einrichtung anzeigen soll.
     */
    private Session mockSessionWithSetUpProcessValue(StatusAttributes setUpProcessValue){
        HashMap<String, Object> attributeMap = new HashMap<>();
            attributeMap.put(StatusAttributes.KEY_PROCESS.toString(), setUpProcessValue.toString());
        return Session.builder()
                .withAttributes(attributeMap)
                .build();

    }



    private IntentRequest mockRequest(String requestName, Slot[] slots,DialogState state){
        Map <String, Slot> slotMap = new HashMap<>();

            for(int i = 0; i< slots.length; i++) {
                if (slots[i] != null) {
                    slotMap.put(slots[i].getName(), slots[i]);
                }
            }


        return IntentRequest.builder()
                .withDialogState(state)
                .withLocale("de-DE")
                .withIntent(Intent.builder()
                        .withName(requestName)
                        .withConfirmationStatus(IntentConfirmationStatus.NONE)
                        .withSlots(slotMap)
                        .build())
                .build();
    }
    private AttributesManager mockAttributesManager(Map<String, Object> sessionList, Map<String, Object> persList){
        AttributesManager attributesManager = Mockito.mock(AttributesManager.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        Map<String, Object> persistantAttributes = new HashMap<>();
        if(sessionList != null)
            sessionAttributes.putAll(sessionList);
        if(persList != null)
            persistantAttributes.putAll(persList);
        when(attributesManager.getSessionAttributes()).thenReturn(sessionAttributes);
        when(attributesManager.getPersistentAttributes()).thenReturn(persistantAttributes);
        return attributesManager;
    }
    private AttributesManager mockAttributesManager(){
        AttributesManager attributesManager = Mockito.mock(AttributesManager.class);
        Map<String,Object> emptyMap = new HashMap<String, Object>();
        when(attributesManager.getSessionAttributes()).thenReturn(emptyMap);
        when(attributesManager.getPersistentAttributes()).thenReturn(emptyMap);
        return attributesManager;
    }




    @Before
    public void setup(){
        handler = new SetUpIntentHandler();
}

    @Test
    public void testCanHandle(){
        final HandlerInput inputMock = Mockito.mock(HandlerInput.class);
        Map<String,Object> sessionList = new HashMap<>();
        sessionList.put(StatusAttributes.KEY_SETUP_IS_COMPLETE.toString(), "false");
        AttributesManager attributesManager = mockAttributesManager(sessionList, null);
        when(inputMock.getAttributesManager()).thenReturn(attributesManager);
        when(inputMock.matches(any())).thenReturn(true);
        assertTrue(handler.canHandle(inputMock));
    }



    /**
     *
     * @return liefert den Input, den der SetUpIntentHandler zum ersten Aufruf bekommt.
     */
    private HandlerInput mockInputSetUpStart(){
        AttributesManager attributesManager = mockAttributesManager();

        Slot[] slots = getSlots(null);
        HandlerInput mockInput = Mockito.mock(HandlerInput.class);
        when(mockInput.getRequestEnvelope()).thenReturn(
                RequestEnvelope.builder()
                        .withRequest(
                                mockRequest("SetUpIntent",slots,DialogState.STARTED))
                        .build());

        when(mockInput.getResponseBuilder()).thenReturn(new ResponseBuilder());
        when(mockInput.getAttributesManager()).thenReturn(attributesManager);
        return mockInput;
    }


    /**
     * dieser Test überprüft den Start der Einrichtung. Falls der User sagt : "Einrichtung starten"
     */
    @Test
    public void testSetUpStart(){
        HandlerInput inputMock = mockInputSetUpStart();
        Response response = handler.handle(inputMock).get();
        assertFalse(response.getShouldEndSession());
        assertEquals("Dialog.ElicitSlot",response.getDirectives().get(0).getType());
        assertTrue(response.getOutputSpeech().toString().contains(OutputStrings.EINRICHTUNG_YES_NO_LOCATION.toString()));
        assertTrue(response.getCard().getType().equals("Simple"));
        assertTrue(response.getCard().toString().contains(OutputStrings.EINRICHTUNG_YES_NO_LOCATION.toString()));
    }

    @Test
    public void testSetUp_notUseLocation(){
        List slotValues = new ArrayList<>();
        slotValues.add("Nein");
        HandlerInput inputMock = mockInputSetUpInProcess(StatusAttributes.VALUE_YES_NO_LOCATION_SET,slotValues);
        Response response = handler.handle(inputMock).get();
        assertFalse(response.getShouldEndSession());
        assertEquals("Dialog.ElicitSlot",response.getDirectives().get(0).getType());
        assertTrue(response.getOutputSpeech().toString().contains(OutputStrings.EINRICHTUNG_HOMEADDRESS.toString()));
        assertTrue(response.getCard().getType().equals("Simple"));
        assertTrue(response.getCard().toString().contains(OutputStrings.EINRICHTUNG_HOMEADDRESS.toString()));
    }
    @Test
    public void testSetUp_doUseLocation(){
        List slotValues = new ArrayList<>();
        slotValues.add("Ja");
        HandlerInput inputMock = mockInputSetUpInProcess(StatusAttributes.VALUE_YES_NO_LOCATION_SET,slotValues);
        //TODO test schreiben wenn feature mit aktuellem standort fertig ist
    }

    private Response getResponse (String adr, StatusAttributes statusAttribute){
        List<String> slotValues = new ArrayList<>();
        slotValues.add("Nein");
        slotValues.add(adr);
        HandlerInput input = mockInputSetUpInProcess(statusAttribute,slotValues);
        return handler.handle(input).get();
    }

    @Test
    public void testSetUp_usePrescribedAddresses(){
        for(int i = 0; i<testAddresses.length;i++){
            System.out.println("testing: " +testAddresses[i]);
            Response response = getResponse(testAddresses[i],StatusAttributes.VALUE_YES_NO_LOCATION_SET);
            assertEquals("Dialog.ConfirmSlot",response.getDirectives().get(0).getType());
            assertTrue(response.getCard().getType().equals("Simple"));
            assertTrue(response.getOutputSpeech().toString().contains("Deine Adresse lautet:"));
            assertTrue(response.getCard().toString().contains("Deine Adresse lautet:"));
        }
    }

    @Test
    public void testStoreData(){
        Map<String, Object> persistantAttributes = new HashMap<String, Object>();
        AttributesManager attributesManager = Mockito.mock(AttributesManager.class);
        ArgumentCaptor<Map> arg = ArgumentCaptor.forClass(Map.class);
        doNothing().when(attributesManager).setPersistentAttributes(arg.capture());
        when(attributesManager.getPersistentAttributes()).thenReturn(persistantAttributes);
        try {
            Address homeAddress = new AddressResolver().getAddressList(profileValues[0]).get(0);
            homeAddress.setName(profileValues[1]);
            Address destinationA = new AddressResolver().getAddressList(profileValues[2]).get(0);
            destinationA.setName(profileValues[3]);
            Address destinationB = new AddressResolver().getAddressList(profileValues[4]).get(0);
            destinationB.setName(profileValues[5]);
            Address destinationC = new AddressResolver().getAddressList(profileValues[6]).get(0);
            destinationC.setName(profileValues[7]);
            persistantAttributes.put("HomeAddress",new ObjectMapper().writeValueAsString(homeAddress));
            persistantAttributes.put("DestinationA",new ObjectMapper().writeValueAsString(destinationA));
            persistantAttributes.put("DestinationB",new ObjectMapper().writeValueAsString(destinationB));
            persistantAttributes.put("DestinationC",new ObjectMapper().writeValueAsString(destinationC));
            attributesManager.setPersistentAttributes(persistantAttributes);

            Map<String,Object> persistantAttributesRestored = attributesManager.getPersistentAttributes();
            Address homeAddressRestored = new ObjectMapper().readValue((String)persistantAttributesRestored.get("HomeAddress"),Address.class);
            Address destinationARestored = new ObjectMapper().readValue((String)persistantAttributesRestored.get("DestinationA"),Address.class);
            Address destinationBRestored = new ObjectMapper().readValue((String)persistantAttributesRestored.get("DestinationB"),Address.class);
            Address destinationCRestored = new ObjectMapper().readValue((String)persistantAttributesRestored.get("DestinationC"),Address.class);

            assertEquals(homeAddress,homeAddressRestored);
            assertEquals(destinationA,destinationARestored);
            assertEquals(destinationB,destinationBRestored);
            assertEquals(destinationC,destinationCRestored);

        } catch(IOException ex){}
    }







}