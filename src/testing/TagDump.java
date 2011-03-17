/*===============================================================================================
 ReadTags Example
 Copyright (c), Firelight Technologies Pty, Ltd 2004-2005.

 This example shows how to read tags from sound files
 ===============================================================================================*/

package testing;

import java.nio.ByteBuffer;

import org.jouvieje.FmodEx.Init;
import org.jouvieje.FmodEx.Defines.INIT_MODES;
import org.jouvieje.FmodEx.Exceptions.InitException;
import org.jouvieje.FmodEx.Misc.BufferUtils;
import org.jouvieje.FmodEx.Misc.PointerUtils;

import org.jouvieje.FmodEx.FmodEx;
import org.jouvieje.FmodEx.Sound;
import org.jouvieje.FmodEx.System;
import org.jouvieje.FmodEx.Enumerations.FMOD_RESULT;
import org.jouvieje.FmodEx.Structures.FMOD_TAG;

import static java.lang.System.out;
import static java.lang.System.*;
import static org.jouvieje.FmodEx.Defines.FMOD_INITFLAGS.*;
import static org.jouvieje.FmodEx.Defines.FMOD_MODE.*;
import static org.jouvieje.FmodEx.Defines.VERSIONS.*;
import static org.jouvieje.FmodEx.Enumerations.FMOD_RESULT.*;
import static org.jouvieje.FmodEx.Enumerations.FMOD_TAGDATATYPE.*;
import static org.jouvieje.FmodEx.Enumerations.FMOD_TAGTYPE.*;

/**
 * I've ported the C++ FmodEx example to NativeFmodEx.
 * 
 * @author Jérôme JOUVIE (Jouvieje)
 * 
 * WANT TO CONTACT ME ? E-mail : jerome.jouvie@gmail.com My web sites :
 * http://topresult.tomato.co.uk/~jerome/ http://jerome.jouvie.free.fr/
 */
public class TagDump {
    private static void ERRCHECK(FMOD_RESULT result) {
        if (result != FMOD_OK) {
            out.printf("FMOD error! (%d) %s\n", result.asInt(), FmodEx.FMOD_ErrorString(result));
            exit(1);
        }
    }

    public static void main(String[] args) {
        /*
         * NativeFmodEx Init
         */
        try {
            Init.loadLibraries(INIT_MODES.INIT_FMOD_EX);
        } catch (InitException e) {
            out.printf("NativeFmodEx error! %s\n", e.getMessage());
            exit(1);
        }

        /*
         * Checking NativeFmodEx version
         */
        if (NATIVEFMODEX_LIBRARY_VERSION != NATIVEFMODEX_JAR_VERSION) {
            out.printf("Error!  NativeFmodEx library version (%08x) is different to jar version (%08x)\n", NATIVEFMODEX_LIBRARY_VERSION,
                    NATIVEFMODEX_JAR_VERSION);
            exit(0);
        }

        /* ================================================== */

        System system = new System();
        Sound sound = new Sound();
        FMOD_RESULT result;
        FMOD_TAG tag = FMOD_TAG.create();
        int numtags;
        int version;

        /*
         * Buffer used to store all datas received from FMOD.
         */
        ByteBuffer buffer = BufferUtils.newByteBuffer(BufferUtils.SIZEOF_INT);

        out.printf("==================================================================\n");
        out.printf("ReadTags Example.  Copyright (c) Firelight Technologies 2004-2005.\n");
        out.printf("==================================================================\n\n");

        /*
         * Create a System object and initialize.
         */
        result = FmodEx.System_Create(system);
        ERRCHECK(result);

        result = system.getVersion(buffer.asIntBuffer());
        ERRCHECK(result);
        version = buffer.getInt(0);

        if (version < FMOD_VERSION) {
            out.printf("Error!  You are using an old version of FMOD %08x.  This program requires %08x\n", version, FMOD_VERSION);
            exit(0);
        }

        result = system.init(100, FMOD_INIT_NORMAL, null);
        ERRCHECK(result);

        /*
         * Open the specified file. Use FMOD_CREATESTREAM and FMOD_OPENONLY so
         * it opens quickly
         */
        result = system.createSound("testfiles/test.ogg", FMOD_SOFTWARE | FMOD_2D | FMOD_CREATESTREAM | FMOD_OPENONLY, null, sound);
        ERRCHECK(result);

        /*
         * Read and display all tags associated with this file
         */
        for (;;) {
            /*
             * An index of -1 means "get the first tag that's new or updated".
             * If no tags are new or updated then getTag will return
             * FMOD_ERR_TAGNOTFOUND. This is the first time we've read any tags
             * so they'll all be new but after we've read them, they won't be
             * new any more.
             */

            if (sound.getTag(null, -1, tag) != FMOD_OK) {
                break;
            }

            if (tag.getDataType() == FMOD_TAGDATATYPE_STRING) {
                out.printf("%s = %s (%d bytes)\n", tag.getName(), PointerUtils.toString(tag.getData()), tag.getDataLen());
            } else {
                out.printf("%s = <binary> (%d bytes)\n", tag.getName(), tag.getDataLen());
            }
        }
        out.printf("\n--end--\n");

        /*
         * Read all the tags regardless of whether they're updated or not. Also
         * show the tag type.
         */

        result = sound.getNumTags(buffer.asIntBuffer(), null);
        ERRCHECK(result);
        numtags = buffer.getInt(0);

        for (int i = 0; i < numtags; i++) {
            result = sound.getTag(null, i, tag);
            ERRCHECK(result);

            // Yeah, if/elseif is a little boring but now we can't use
            // switch/case
            if (tag.getType() == FMOD_TAGTYPE_UNKNOWN) {
                out.printf("FMOD_TAGTYPE_UNKNOWN  ");
            } else if (tag.getType() == FMOD_TAGTYPE_ID3V1) {
                out.printf("FMOD_TAGTYPE_ID3V1  ");
            } else if (tag.getType() == FMOD_TAGTYPE_ID3V2) {
                out.printf("FMOD_TAGTYPE_ID3V2  ");
            } else if (tag.getType() == FMOD_TAGTYPE_VORBISCOMMENT) {
                out.printf("FMOD_TAGTYPE_VORBISCOMMENT  ");
            } else if (tag.getType() == FMOD_TAGTYPE_SHOUTCAST) {
                out.printf("FMOD_TAGTYPE_SHOUTCAST  ");
            } else if (tag.getType() == FMOD_TAGTYPE_ICECAST) {
                out.printf("FMOD_TAGTYPE_ICECAST  ");
            } else if (tag.getType() == FMOD_TAGTYPE_ASF) {
                out.printf("FMOD_TAGTYPE_ASF  ");
            } else if (tag.getType() == FMOD_TAGTYPE_FMOD) {
                out.printf("FdMOD_TAGTYPE_FMOD  ");
            } else if (tag.getType() == FMOD_TAGTYPE_USER) {
                out.printf("FMOD_TAGTYPE_USER  ");
            }

            if (tag.getDataType() == FMOD_TAGDATATYPE_STRING) {
                out.printf("%s = %s (%d bytes)\n", tag.getName(), PointerUtils.toString(tag.getData()), tag.getDataLen());
            } else {
                out.printf("%s = ??? (%d bytes)\n", tag.getName(), tag.getDataLen());
            }
        }
        out.printf("\n");

        /*
         * Find a specific tag by name. Specify an index > 0 to get access to
         * multiple tags of the same name.
         */
        result = sound.getTag("ARTIST", 0, tag);
        ERRCHECK(result);
        out.printf("%s = %s (%d bytes)\n", tag.getName(), PointerUtils.toString(tag.getData()), tag.getDataLen());
        out.printf("\n");

        /*
         * Shut down
         */
        result = sound.release();
        ERRCHECK(result);
        result = system.close();
        ERRCHECK(result);
        result = system.release();
        ERRCHECK(result);

        exit(0);
    }
}