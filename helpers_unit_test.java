
public class HelpersUnitTest {
	
	@Test
    public void jsonConverterTest(){
        //example of toJson
        ParameterDTO parameterDTO = new ParameterDTO();
        parameterDTO.setId(12l);
        parameterDTO.setParameterName("test");
        String json = (String) Helpers.jsonConverter(true,parameterDTO,null);
        Assert.assertNotNull(json);

        //jsonToObject
        String jsonStr = "{\"id\":12,\"filterName\":null,\"version\":null,\"parameterCode\":null,\"parameterName\":\"test\",\"parameterText\":null,\"parameterValue\":null,\"parameterParentCode\":null}";
        ParameterDTO parameterDTOObj = (ParameterDTO) Helpers.jsonConverter(false,ParameterDTO.class,jsonStr);
        Assert.assertNotNull(parameterDTOObj);
    }
	
	@Test
    public void convertCurrencyTEST(){
        Assert.assertTrue(Helpers.convertCurrency("0.500","bigdecimal",2).equals(new BigDecimal("0.50")));
        Assert.assertTrue(Helpers.convertCurrency("20000,25","bigdecimal",2).equals(new BigDecimal("20000.25")));
        Assert.assertTrue(Helpers.convertCurrency("200.000.000,25","bigdecimal",2).equals(new BigDecimal("200000000.25")));
        Assert.assertTrue(Helpers.convertCurrency("20.000,15","bigdecimal",2).equals(new BigDecimal("20000.15")));
        Assert.assertTrue(Helpers.convertCurrency("200,000,000.25","bigdecimal",2).equals(new BigDecimal("200000000.25")));
        Assert.assertTrue(Helpers.convertCurrency("20,000.15","bigdecimal",2).equals(new BigDecimal("20000.15")));
        Assert.assertTrue(Helpers.convertCurrency("200.200.200","bigdecimal",null).equals(new BigDecimal("200200200")));
        Assert.assertTrue(Helpers.convertCurrency("200,200,200","bigdecimal",0).equals(new BigDecimal("200200200")));
        Assert.assertTrue(Helpers.convertCurrency("0.123","bigdecimal",3).equals(new BigDecimal("0.123")));
        Assert.assertTrue(Helpers.convertCurrency("0,123","bigdecimal",3).equals(new BigDecimal("0.123")));
        Assert.assertTrue(Helpers.convertCurrency("00,000,00","bigdecimal",0).equals(new BigDecimal("0")));
        Assert.assertTrue(Helpers.convertCurrency("00.000.00","bigdecimal",0).equals(new BigDecimal("0")));
        Assert.assertTrue(Helpers.convertCurrency("00","bigdecimal",0).equals(new BigDecimal("0")));
        Assert.assertTrue(Helpers.convertCurrency(",123","bigdecimal",2).equals(new BigDecimal("0.12")));
        Assert.assertTrue(Helpers.convertCurrency(".123","bigdecimal",2).equals(new BigDecimal("0.12")));
        Assert.assertTrue(Helpers.convertCurrency(",123","bigdecimal",2).equals(new BigDecimal("0.12")));
        Assert.assertTrue(Helpers.convertCurrency("221681.48","bigdecimal",null).equals(new BigDecimal("221681.48")));
        Assert.assertTrue(Helpers.convertCurrency(221681.48,"bigdecimal",null).equals(new BigDecimal("221681.48")));
        Assert.assertTrue(Helpers.convertCurrency(".1","double",2).equals(new Double("0.10")));
        Assert.assertTrue(Helpers.convertCurrency(new Double(221681.4),"float",null).equals(new Float("221681.4")));
        Assert.assertTrue(Helpers.convertCurrency(",","bigdecimal",2) == null);
    }
	
	
	@Test
    public void checkStrDateFormatTEST(){
        Assert.assertTrue(checkStrDateFormat("2028.12.12","yyyy.MM.dd"));
		Assert.assertTrue(checkStrDateFormat("22-12-2028","dd-MM-yyyy"));
    }
	
	@Test
    public void isIncludeInArrTEST(){
        Assert.assertTrue(Helpers.isIncludeInArr(new String[]{"a","b","c"},true,new String[]{"a","b","c"},true,true));
        Assert.assertTrue(Helpers.isIncludeInArr(new String[]{"a","b","c"},true,new String[]{"c","b","a"},false,false));
        Assert.assertTrue(Helpers.isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"a","b","c","d"},true,true));
        Assert.assertTrue(Helpers.isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"b","a","c","d"},false,true));
        Assert.assertFalse(Helpers.isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"B","a","c","d"},false,true));
    }
	
	@Test
    public void convertDateTimeFormatTest(){
        //if date null, default date is current
        //return pattern is only for string return type
        Assert.assertNotNull(Helpers.convertDateTimeFormat(null,Date.class,null,null,"istanbul"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(new java.util.Date(2023,9,8,7,6,5), String.class, null,"dd.MM.yyyy","istanbul").equals("08.09.2023"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(java.time.LocalDateTime.of(2012,9,8,7,6,5,4), String.class, null,"yyyy.MM.dd'T'HH:mm:ss","istanbul").equals("2012.09.08T07:06:05"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(java.time.OffsetDateTime.of(2012,9,8,7,6,5,4, ZoneOffset.ofHours(2)), String.class, null,"dd.MM.yyyy'T'HH:mm","istanbul").equals("08.09.2012T07:06"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat("08.09.2012T07:06", java.time.LocalDateTime.class, true,null,"istanbul*").equals(java.time.LocalDateTime.of(2012,9,8,7,6,0,0)));
        Assert.assertNotNull(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(null,Date.class,null,null,"istanbul"));
        Assert.assertNotNull(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(null, java.time.LocalDateTime.class, null,null,"istanbul"));
        Assert.assertNotNull(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(null, String.class, null,"dd.MM.yyyy","istanbul"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(java.time.LocalDate.of(2012,9,8), String.class, null,"dd.MM.yyyy","istanbul").equals("08.09.2012"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(new java.util.GregorianCalendar(2023, 9,8,7,6,5), String.class, null,"dd.MM.yyyy","istanbul*").equals("08.09.2023"));
        Assert.assertTrue(rally.services.mobileapproval.util.Helpers.convertDateTimeFormat(new java.sql.Date(2012,9,8), String.class, null,"dd.MM.yyyy","istanbul").equals("08.09.2012"));
    }
}