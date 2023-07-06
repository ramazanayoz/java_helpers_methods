/*
	*Methods
	-Object jsonConverter(boolean toJson, Object obj, String json)
	-Object convertCurrency("0.500","bigdecimal",2)
	-boolean checkStrDateFormat(String date, String pattern)
	-<T> List<T> callAsAsync(Object obj, Object service, String methodName, int thread)
	-String callSoapService(String soapRequest, String endpoint)
*/
	
	
	
	
public class Helpers{	
	
	/*
	*
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
	*
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
	*
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
	*
	*
	*callSoapService("<?xml version=\"1.0\"?> <soap:Envelope...","https://soapEndpoint.com")
	*/
	public static String callSoapService(String soapRequest, String endpoint) {
        try {
            String securityTag = getSecuritySoapTag();
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
	
}

