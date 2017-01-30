//package sandbox.sfwatergit.utils.pt.gtfsUtils.ptMatrix;
//
//import sandbox.sfwatergit.PeerPressureConstants;
//import sandbox.sfwatergit.utils.pt.gtfsUtils.GtfsPropertyManager;
//
///**
// * @author gabriel.thunig
// */
//public class RunOTPMatrixRouter {
//
//    public static void main(String[] args) {
//        GtfsPropertyManager props = new GtfsPropertyManager();
//        final String outputDir = props.getOutputDir();
//        String fromIndividualsFilePath = String.format("%sptStops.csv", outputDir);
//        String toIndividualsFilePath = String.format("%sptStops.csv", outputDir);
//        String graphParentDirectoryPath = "/Users/sfeygin/Documents/data/osm/otp/";
//        String timeZone = "America/Los_Angeles";
//        String date = "2016-11-5";
//        String departureTime = Integer.toString(8*60*60);  // 8:00 am
//        String inputCRS = PeerPressureConstants.SC_CRS;
//        String outputCRS = PeerPressureConstants.SC_CRS;
//        String[] arguments = new String[9];
//        arguments[0] = fromIndividualsFilePath;
//        arguments[1] = toIndividualsFilePath;
//        arguments[2] = graphParentDirectoryPath;
//        arguments[3] = outputDir;
//        arguments[4] = timeZone;
//        arguments[5] = date;
//        arguments[6] = departureTime;
//        arguments[7] = inputCRS;
//        arguments[8] = outputCRS;
//
//        OTPMatrixRouter.main(arguments);
//    }
//}
