package data.dragndrop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

import data.TrackIdType;

public class TrackIdTransfer extends ByteArrayTransfer {

    private static final String MYTYPENAME = "track_id_transfer";
    private static final int MYTYPEID = registerType(MYTYPENAME);
    private static TrackIdTransfer _instance = new TrackIdTransfer();

    public static TrackIdTransfer getInstance() {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (!checkMyType(object) || !isSupportedType(transferData)) {
            DND.error(DND.ERROR_INVALID_DATA);
        }
        TrackIdType[] myTypes = (TrackIdType[]) object;
        try {
            // write data to a byte array and then ask super to convert to
            // pMedium
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream writeOut = new DataOutputStream(out);
            for (int i = 0, length = myTypes.length; i < length; i++) {
                writeOut.write(myTypes[i].trackId);
            }
            byte[] buffer = out.toByteArray();
            writeOut.close();
            super.javaToNative(buffer, transferData);
        } catch (IOException e) {
        }
    }

    public Object nativeToJava(TransferData transferData) {
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;

            TrackIdType[] myData = new TrackIdType[0];
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);             
                
                while (readIn.available() > 20) {
                    TrackIdType datum = new TrackIdType();
                    datum.trackId = readIn.readInt();
                    TrackIdType[] newMyData = new TrackIdType[myData.length + 1];
                    System.arraycopy(myData, 0, newMyData, 0, myData.length);
                    newMyData[myData.length] = datum;
                    myData = newMyData;
                }
                readIn.close();
            } catch (IOException ex) {
                return null;
            }
            return myData;
        }

        return null;
    }

    protected String[] getTypeNames() {
        return new String[] { MYTYPENAME };
    }

    protected int[] getTypeIds() {
        return new int[] { MYTYPEID };
    }

    boolean checkMyType(Object object) {
        if (object == null || !(object instanceof TrackIdType[])
                || ((TrackIdType[]) object).length == 0) {
            return false;
        }
        TrackIdType[] myTypes = (TrackIdType[]) object;
        for (int i = 0; i < myTypes.length; i++) {
            if (myTypes[i] == null || myTypes[i].trackId <= 0) {
                return false;
            }
        }
        return true;
    }

    protected boolean validate(Object object) {
        return checkMyType(object);
    }
}
