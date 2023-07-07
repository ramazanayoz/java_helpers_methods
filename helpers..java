/*
*Methods
-Object jsonConverter(boolean toJson, Object obj, String json)
-Object convertCurrency("0.500","bigdecimal",2)
-boolean checkStrDateFormat(String date, String pattern)
-<T> List<T> callAsAsync(Object obj, Object service, String methodName, int thread)
-String callSoapService(String soapRequest, String endpoint)
-boolean isIncludeInArr(String[] values, Boolean isAraySizeEqual, String[] searched, Boolean inSameOrder, Boolean isCaseSensetive)
-Object convertDateTimeFormat(Object date, Object returnType, Boolean isYearEnd, String returnPattern, final String zoneName)
-boolean isCurrencyProvideLimit(String value, Integer scale, String bigLimit, String smallLimit)
*/
	
	
public class Helpers{	
	
	/*
	*
	*
	*
	*example of toJson
	*String json = (String) Helpers.jsonConverter(true,randomObj,null);
	*
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
	*
	*
	*
	*convertCurrency("0.500","bigdecimal",2) -> new BigDecimal("0.50" ) 
	*convertCurrency("20000,25","bigdecimal",2) -> new BigDecimal("20000.25" ) 
	*convertCurrency("200.000.000,25","bigdecimal",2) -> new BigDecimal("200000000.25" ) 
	*convertCurrency("20.000,15","bigdecimal",2) -> new BigDecimal("20000.15" ) 
	*convertCurrency("200,000,000.25","bigdecimal",2) -> new BigDecimal("200000000.25" )
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
	*
	*
	*
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
	
	/*
	*
	*
	*
	*List<CustomerResponseDTO> = callAsAsync(dtoList ,getCustomerServiceV16Provider,"transferAdd",50)
	*endpoint is fetching async by getCustomerServiceV16Provider service with request list
	*/
	public static <T> List<T> callAsAsync(Object obj, Object service, String methodName, int thread) {
        List objAsList = null;
        ArrayList<T> responseDTO = new ArrayList<T>();
        if (obj.getClass().isArray() || obj instanceof Collection) {
            objAsList = (List<T>) obj;
        } else {
            objAsList = new ArrayList<T>() {{
                add((T) obj);
            }};
        }
        log.info("CallingAsAsync {} method in {} service times {}", methodName, service.getClass().getTypeName(), objAsList.size());
        try {
            final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(thread));
            final List<ListenableFuture<T>> listenableFutures = Lists.newArrayListWithExpectedSize(objAsList.size());
            Method method = service.getClass().getMethod(methodName, objAsList.get(0).getClass());
            for (int i = 0; i < objAsList.size(); i++) {
                int finalI = i;
                List finalObjAsList = objAsList;
                final Callable<T> asyncTask = new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        return (T) method.invoke(service, finalObjAsList.get(finalI));
                    }
                };
                listenableFutures.add(executor.submit(asyncTask));
            }

            final int[] counter = {0};

            listenableFutures.parallelStream().forEach(listenableFuture -> {
                Futures.addCallback(listenableFuture, new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T result) {
                        responseDTO.add(result);
                        counter[0]++;
                    }

                    @Override
                    public void onFailure(final Throwable thrown) {
                        log.info("callback failed exception:{} ", thrown.getMessage());
                        counter[0]++;
                    }
                }, executor);
            });
            while (counter[0] < objAsList.size()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
            executor.shutdown();
            executor.shutdownNow();
            log.info("Existed from CallingAsAsync {} method in {} service", methodName, service.getClass().getTypeName());
        } catch (Exception e) {
            log.info("Exception from CallingAsAsync {} method in {} service. Exception message:{}", methodName, service.getClass().getTypeName(), e.getMessage());
            throw new RuntimeException(e);
        }
        return responseDTO;
    }
	
	/*
	*
	*
	*
	*callSoapService("<?xml version=\"1.0\"?> <soap:Envelope...","https://soapEndpoint.com")
	*/
    public static String callSoapService(String soapRequest, String endpoint) {
        try {
            var getSecuritySoapTag = new Object() {
                private String getSecuritySoapTag() {
                    String securitySoapTag = null;
                    MessageFactory messageFactory = null;
                    try {
                        messageFactory = MessageFactory.newInstance();
                        SOAPMessage soapMessage = messageFactory.createMessage();
                        soapMessage.getSOAPHeader().addChildElement(SOAPHeaderHandler.createSecurityHeader());
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        soapMessage.writeTo(bos);
                        String soapMessageStr = new String(bos.toByteArray());
                        Pattern pattern = Pattern.compile("<wsse:Security(.*?)</wsse:Security>", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(soapMessageStr);
                        if (matcher.find()) {
                            securitySoapTag = matcher.group().toString();
                        }
                        SOAPHeaderHandler.createSecurityHeader();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return securitySoapTag;

                }
            };

            String securityTag = getSecuritySoapTag.getSecuritySoapTag(); //optional
            String headerTag = "<s:Header>";
            soapRequest = soapRequest.replaceAll("<\\?xml.+?>", "");
            String headerTagEnd = "</s:Header>";
            String url = endpoint; // replace your URL here
            if (securityTag == null) {
                return null;
            }
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // change these values as per soapui request on top left of request, click on RAW, you will find all the headers
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            Pattern pattern = Pattern.compile(headerTag + "(.*?)" + headerTagEnd, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(soapRequest);
            //if header tag exist
            if (matcher.find()) {
                //if security tag exist
                if (matcher.group(1).contains("<wsse:Security")) {
                    //if binarySecurityToken tag exist
                    if (soapRequest.contains("BinarySecurityToken")) {
                        soapRequest = soapRequest.replaceAll("<wsse:BinarySecurityToken(.*?)BinarySecurityToken>", getLtpaToken());
                    } else {
                        soapRequest = soapRequest.replaceAll("<wsse:Security(.*?)wsse:Security>", securityTag);
                    }
                } else {
                    soapRequest = soapRequest.substring(0, soapRequest.indexOf(headerTag) + headerTag.length()) + securityTag + soapRequest.substring(soapRequest.indexOf(headerTag) + headerTag.length() + 1, soapRequest.length());
                }
                wr.writeBytes(soapRequest);
                wr.flush();
                wr.close();
                String responseStatus = con.getResponseMessage();
                System.out.println(responseStatus);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // You can play with response which is available as string now:
                String finalvalue = response.toString();
                // or you can parse/substring the required tag from response as below based your response code
                finalvalue = finalvalue.substring(finalvalue.indexOf("<response>") + 10, finalvalue.indexOf("</response>"));

                return finalvalue;
            }
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }
	
	/*
	*
	*
	*
	*isIncludeInArr(new String[]{"a","b","c"},true,new String[]{"a","b","c"},true,true)); --> true
	*isIncludeInArr(new String[]{"a","b","c"},true,new String[]{"c","b","a"},false,false)); --> true
	*isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"a","b","c","d"},true,true)); --> true
	*isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"b","a","c","d"},false,true)); --> true
	*isIncludeInArr(new String[]{"a","b","c"},false,new String[]{"B","a","c","d"},false,true)); --> false
	*/
	public static boolean isIncludeInArr(String[] values, Boolean isAraySizeEqual, String[] searched, Boolean inSameOrder, Boolean isCaseSensetive) {
        try {
            List<String> valueList = Arrays.asList(values);
            List<String> searchedList = Arrays.asList(searched);

            if (isAraySizeEqual == null) {
                isAraySizeEqual = false;
            }
            if (inSameOrder == null) {
                inSameOrder = false;
            }
            if (isCaseSensetive == null) {
                isCaseSensetive = true;
            }
            if (isCaseSensetive == false) {
                valueList.replaceAll(String::toLowerCase);
                searchedList.replaceAll(String::toLowerCase);
            }

            if (!isAraySizeEqual && !inSameOrder) {
                for (int i = 0; i < valueList.size(); i++) {
                    if (!searchedList.contains(valueList.get(i).trim())) {
                        return false;
                    }
                }
                return true;
            } else if (!isAraySizeEqual && inSameOrder) {
                for (int i = 0; i < valueList.size(); i++) {
                    if (!valueList.get(i).trim().equals(searchedList.get(i).trim())) {
                        return false;
                    }
                }
                return true;
            } else if (isAraySizeEqual && inSameOrder) {
                if (!(valueList.size() == searchedList.size())) {
                    return false;
                }
                for (int i = 0; i < valueList.size(); i++) {
                    if (!valueList.get(i).trim().equals(searchedList.get(i).trim())) {
                        return false;
                    }
                    ;
                }
                return true;
            } else if (isAraySizeEqual && !inSameOrder) {
                if (!(valueList.size() == searchedList.size())) {
                    return false;
                }
                for (int i = 0; i < valueList.size(); i++) {
                    if (!searchedList.contains(valueList.get(i).trim())) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
	
	/*
	*
	*
	*
	*
	*if date null, default date is current
    *return pattern is only for string return type
	*
	*convertDateTimeFormat(null,java.util.Date.class,null,null,"istanbul"));	//current date as Date obj
	*convertDateTimeFormat(null, java.time.LocalDateTime.class, null,null,"istanbul")); //current date as LocalDate obj
	*convertDateTimeFormat(null, String.class, null,"dd.MM.yyyy","istanbul"));	//current time as String in specific format
	*
	*convertDateTimeFormat("08.09.2012T07:06", java.time.LocalDateTime.class, true,null,"istanbul") --> java.time.LocalDateTime.of(2012,9,8,7,6,0,0);
	*
	*convertDateTimeFormat(new java.util.Date(2023,9,8,7,6,5), String.class, null,"dd.MM.yyyy","istanbul") --> "08.09.2023";
	*convertDateTimeFormat(new java.util.GregorianCalendar(2023, 9,8,7,6,5), String.class, null,"dd.MM.yyyy","istanbul") --> "08.09.2023";
	*convertDateTimeFormat(new java.sql.Date(2012,9,8), String.class, null,"dd.MM.yyyy","istanbul") --> "08.09.2012";
	*convertDateTimeFormat(java.time.OffsetDateTime.of(2012,9,8,7,6,5,4, ZoneOffset.ofHours(2)), String.class, null,"dd.MM.yyyy'T'HH:mm","istanbul") -->"08.09.2012T07:06";
	*/
    public static Object convertDateTimeFormat(Object date, Object returnType, Boolean isYearEnd, String returnPattern, final String zoneName) {
        var getTimeInDefaultFormat = new Object(){
            private String getTimeInDefaultFormat(String time) {
                String[] splitTime = time.split(":");
                if (time == null) {
                    return "T00:00:00.000000000";
                } else if (splitTime.length == 1) {
                    return "T" + StringUtils.leftPad(splitTime[0], 2, "0") + ":00:00.000000000";
                } else if (splitTime.length == 2) {
                    return "T" + StringUtils.leftPad(splitTime[0], 2, "0") + ":" + StringUtils.leftPad(splitTime[1], 2, "0") + ":00.000000000";
                } else if (splitTime.length == 3) {
                    if (time.split("\\.").length > 1) {
                        return "T" + time;
                    }
                    return "T" + StringUtils.leftPad(splitTime[0], 2, "0") + ":" + StringUtils.leftPad(splitTime[1], 2, "0") + ":" + StringUtils.leftPad(splitTime[2], 2, "0") + ".000000000";
                } else {
                    return "T" + time;
                }
            }
        };
        
        Object resultDate = null;
        String finalZoneName;
        ZoneId zoneId = ZoneId.systemDefault();
        DateTimeFormatter formatter;
        String stringFormat;
        Object localDateFormat;
        String finalDate;
        String finalTime = null;

        if (zoneName != null && zoneName.contains("*")) {
            finalZoneName = ZoneOffset.getAvailableZoneIds()
                    .parallelStream().limit(1)
                    .filter(x -> x.toLowerCase().trim().contains(zoneName.trim().replaceAll("\\*", "").toLowerCase()))
                    .findFirst()
                    .orElse(ZoneId.systemDefault().getId());
            zoneId = ZoneId.of(finalZoneName);
        }
        try {
            if (date == null) {
                date = Instant.now().atZone(ZoneId.of("Europe/Istanbul"));
            }

            String dateClass = date.getClass().toString().replace("class ", "");
            String returnTypeClass = returnType.toString().replaceAll("class", "").trim();

            if (dateClass.equals("java.util.GregorianCalendar")) {
                String year = ((java.util.GregorianCalendar) date).toZonedDateTime().getYear() + "";
                String month = ((java.util.GregorianCalendar) date).getTime().getMonth() + "";
                String day = ((java.util.GregorianCalendar) date).getTime().getDate() + "";
                String hours = ((java.util.GregorianCalendar) date).getTime().getHours() + "";
                String minutes = ((java.util.GregorianCalendar) date).getTime().getMinutes() + "";
                String seconds = ((java.util.GregorianCalendar) date).getTime().getSeconds() + "";
                finalDate = year + "-" + StringUtils.leftPad(month, 2, "0") + "-" + StringUtils.leftPad(day, 2, "0");
                finalTime = "T" + StringUtils.leftPad(hours, 2, "0") + ":" + StringUtils.leftPad(minutes, 2, "0") + ":" + StringUtils.leftPad(seconds, 2, "0") + ".00";
            } else if (dateClass.equals("java.sql.Date")) {
                String year = ((java.sql.Date) date).getYear() + "";
                String month = ((java.sql.Date) date).getMonth() + "";
                String day = ((java.sql.Date) date).getDate() + "";
                finalDate = year + "-" + StringUtils.leftPad(month, 2, "0") + "-" + StringUtils.leftPad(day, 2, "0");
            } else if (dateClass.equals("java.util.Date")) {
                String year = ((java.util.Date) date).getYear() + "";
                if (Integer.valueOf(year) < 1000) {
                    year = ((java.util.Date) date).toString().substring(((java.util.Date) date).toString().lastIndexOf(' ') + 1) + "";
                }
                String month = ((java.util.Date) date).getMonth() + "";
                String day = ((java.util.Date) date).getDate() + "";
                String hours = ((java.util.Date) date).getHours() + "";
                String minutes = ((java.util.Date) date).getMinutes() + "";
                String seconds = ((java.util.Date) date).getSeconds() + "";
                finalDate = year + "-" + StringUtils.leftPad(month, 2, "0") + "-" + StringUtils.leftPad(day, 2, "0");
                finalTime = "T" + StringUtils.leftPad(hours, 2, "0") + ":" + StringUtils.leftPad(minutes, 2, "0") + ":" + StringUtils.leftPad(seconds, 2, "0") + ".00";
            } else {
                //converting to localDateFormat format part
                String trimmedDate = date.toString().trim().split("T")[0];
                finalDate = getDateInDefaultFormat(trimmedDate, isYearEnd);
                if (date.toString().trim().split("T").length > 1) {
                    finalTime = date.toString().trim().split("T")[1];
                    if (dateClass.equalsIgnoreCase("java.lang.String")) {
                        finalTime = getTimeInDefaultFormat.getTimeInDefaultFormat(finalTime).split("\\+")[0];
                    } else {
                        finalTime = "T" + finalTime.split("\\+")[0];
                    }
                } else {
                    finalTime = "T00:00:00.00";
                }
            }
            if (finalTime == null) {
                finalTime = "T00:00:00.00";
            }
            if (returnPattern == null) {
                stringFormat = finalDate + finalTime;
            } else {
                localDateFormat = LocalDateTime.parse(finalDate + finalTime);

                //localDateFormat convert to string
                formatter = DateTimeFormatter.ofPattern(returnPattern).withZone(zoneId);
                stringFormat = formatter.format((TemporalAccessor) localDateFormat);
            }

            ///////////////////////////////
            //return part
            if (returnTypeClass.contains("java.lang.String")) {
                resultDate = stringFormat;
            } else if (returnTypeClass.contains("java.time.Instant")) {
            } else if (returnTypeClass.contains("java.time.OffsetDateTime")) {
                resultDate = OffsetDateTime.parse(stringFormat);
            } else if (returnTypeClass.equals("java.time.ZonedDateTime")) {
            } else if (returnTypeClass.contains("java.time.LocalDateTime")) {
                resultDate = java.time.LocalDateTime.parse(stringFormat);
            } else if (returnTypeClass.equals("java.time.LocalDate")) {
                resultDate = java.time.LocalDate.parse(finalDate);
            } else if (returnTypeClass.equals("java.time.LocalTime")) {
            } else if (returnTypeClass.equals("java.time.OffsetTime")) {
            } else if (returnTypeClass.equals("java.util.Date")) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                resultDate = df.parse(stringFormat);
            } else if (returnTypeClass.equals("java.util.GregorianCalendar")) {
            } else if (returnTypeClass.equals("javax.xml.datatype.XMLGregorianCalendar")) {
                resultDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(finalDate);
            } else if (returnTypeClass.equals("java.sql.Date")) {
            } else if (returnTypeClass.equals("java.sql.Time")) {
            } else if (returnTypeClass.equals("java.sql.Timestamp")) {
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            return resultDate;
        }
    }
	
	/*
	*
	*
	*
	*Helpers.isCurrencyProvideLimit("20.123",2,"20.122","0") //TRUE
    *Helpers.isCurrencyProvideLimit("20.123",3,"20.122","0") //FALSE
    *Helpers.isCurrencyProvideLimit("20.123",0,"20.122","0") //TRUE
    *Helpers.isCurrencyProvideLimit("0.123",3,"20","0.122") //FALSE
	*/
	public static boolean isCurrencyProvideLimit(String value, Integer scale, String bigLimit, String smallLimit) {
        try {
            boolean result = true;
            if (value == null || value.isEmpty()) {
                return false;
            }
            if(scale == null){
                scale = 2; //default
            }
            BigDecimal smallLimitDec = new BigDecimal(Double.parseDouble(smallLimit)).setScale(scale, RoundingMode.HALF_DOWN);
            BigDecimal bigLimitDesc = new BigDecimal(Double.parseDouble(bigLimit)).setScale(scale, RoundingMode.HALF_DOWN);
            BigDecimal valueDec = new BigDecimal(Double.parseDouble(value)).setScale(scale, RoundingMode.HALF_DOWN);
            if(smallLimit != null) {
                if (((BigDecimal) convertCurrency(valueDec.toString(), "bigdecimal", 2)).compareTo(smallLimitDec) >= 0) {
                } else {
                    return false;
                }
            }if(bigLimit != null){
                if (((BigDecimal) convertCurrency(valueDec.toString(), "bigdecimal", 2)).compareTo(bigLimitDesc) >= 0) {
                } else {
                    return false;
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

