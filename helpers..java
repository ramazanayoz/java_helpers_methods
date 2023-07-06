/*
	*Methods
	-Object jsonConverter(boolean toJson, Object obj, String json)
	-Object convertCurrency("0.500","bigdecimal",2)
	-boolean checkStrDateFormat(String date, String pattern)
*/
	
	
	
	
public class Helpers{	
	
	/*
		*example of toJson
		*String json = (String) Helpers.jsonConverter(true,randomObj,null);
		
		*example of jsonToObject
		*ParameterDTO parameterDTO = (ParameterDTO) Helpers.jsonConverter(false,ParameterDTO.class,jsonStr);
	*/
	public static Object jsonConverter(boolean toJson, Object obj, String json){
        if(toJson){
            ObjectWriter ow = new ObjectMapper().writer();
            json = "";
            try {
                json = String.valueOf(ow.writeValueAsString(obj));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return json;
        }else{
            try {
                ObjectMapper mapper = new ObjectMapper();
                //mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                //.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                if(obj.toString().contains("class ")){
                    return mapper.readValue(json, (Class) obj);
                }else{
                    return mapper.readValue(json, obj.getClass());
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
	
	/*
	*convertCurrency("0.500","bigdecimal",2) -> new BigDecimal("0.50" ) 
	*convertCurrency("20000,25","bigdecimal",2) -> new BigDecimal("20000.25" ) 
	*convertCurrency("200.000.000,25","bigdecimal",2) -> new BigDecimal("200000000.25" ) 
	*convertCurrency("20.000,15","bigdecimal",2) -> new BigDecimal("20000.15" ) 
	*convertCurrency("200,000,000.25","bigdecimal",2) -> new BigDecimal("200000000.25" )
	*
	*
	*convertCurrency(221681.48,"bigdecimal",null)-> new BigDecimal("221681.48" ) 
	*convertCurrency(".1","double",2) -> new Double("0.10" ) 
	*convertCurrency(new Double(221681.4),"float",null)-> new Float("221681.4" ) 
	*convertCurrency(",","bigdecimal",2) -> null
	*/
	public static Object convertCurrency(Object currency, String type, Integer scale) {
        String strCurrency = String.valueOf(currency);
        try {
            if (strCurrency == null || strCurrency.isEmpty()) {
                return null;
            }
            if (type != null) {
                type = type.toLowerCase().trim();
            }

            strCurrency = strCurrency.trim();
            if (strCurrency.charAt(0) == '.' || strCurrency.charAt(0) == ',') {
                if (strCurrency.length() > 1) {
                    strCurrency = String.format("%1$" + (strCurrency.length() + 1) + "s", strCurrency).replace(' ', '0').replaceAll(",", ".");
                } else {
                    return null;
                }
            } else if (strCurrency.split("\\.").length == 1 && strCurrency.split(",").length > 1) {  //20000,25 || 200,200,200
                if (strCurrency.split(",").length > 2) {
                    strCurrency = strCurrency.replaceAll(",", "");
                } else {
                    strCurrency = strCurrency.replaceAll(",", ".");
                }
            } else if (strCurrency.split("\\.").length > 1 && strCurrency.split(",").length == 1) {  //20000.25 || 200.250.250
                if (strCurrency.split("\\.").length > 2) {
                    strCurrency = strCurrency.replaceAll("\\.", "");
                }
            } else if (strCurrency.split("\\.").length > strCurrency.split(",").length || strCurrency.indexOf(".") < strCurrency.indexOf(",")) {    //200.000.000,25 and 20.000,15
                strCurrency = strCurrency.replaceAll("\\.", "").replaceAll(",", ".");
            } else if (strCurrency.split(",").length > strCurrency.split("\\.").length || strCurrency.indexOf(",") < strCurrency.indexOf(".")) {    //200,000,000.25 and 20,000.15
                strCurrency = strCurrency.replaceAll(",", "");
            }
            //for return type bigDecimal
            if (type != null && type.equals("bigdecimal")) {
                if (scale == null) {
                    return new BigDecimal(strCurrency);
                }
                return new BigDecimal(strCurrency).setScale(scale, RoundingMode.HALF_DOWN);
            }
            //for return type float
            else if (type != null && type.equals("float")) {
                return Float.parseFloat(strCurrency);
            }
            //for return type double
            else if (type != null && type.equals("double")) {
                return Double.parseDouble(strCurrency);
            }
            //for return type string
            else {
                DecimalFormat formatter = (DecimalFormat) NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
                DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
                symbols.setCurrencySymbol(""); // Don't use null.
                formatter.setDecimalFormatSymbols(symbols);
                return formatter.format(Double.valueOf(strCurrency));
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	/*
	*checkStrDateFormat("2028.12.12","yyyy.MM.dd") -> true
	*checkStrDateFormat("22-12-2028","dd-MM-yyyy") -> true
	*/
	public static boolean checkStrDateFormat(String date, String pattern) {
        try {
            if (date == null || date.isEmpty() || pattern == null || pattern.isEmpty()) {
                return false;
            }
            date = date.trim();
            pattern = pattern.trim();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.SMART);
            dateTimeFormatter.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return false;
        }
    }
	
}