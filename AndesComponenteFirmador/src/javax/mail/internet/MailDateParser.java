/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.internet;

import java.text.ParseException;

/**
 *
 * @author abarrime
 */
class MailDateParser {

    int index = 0;
    char[] orig = null;

    public MailDateParser() {
        this.orig = new char[1000];
        this.index = 0;
    }

    public MailDateParser(char[] orig, int index) {
        this.orig = orig;
        this.index = index;
    }

    public void skipUntilNumber() throws ParseException {
        try {
            while (true) {
                switch (this.orig[this.index]) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return;
                }

                this.index++;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("No Number Found", this.index);
        }
    }

    public void skipWhiteSpace() {
        int len = this.orig.length;
        while (this.index < len) {
            switch (this.orig[this.index]) {
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    this.index++;
                    continue;
            }
            return;
        }
    }

    public int peekChar() throws ParseException {
        if (this.index < this.orig.length) {
            return this.orig[this.index];
        }
        throw new ParseException("No more characters", this.index);
    }

    public void skipChar(char c) throws ParseException {
        if (this.index < this.orig.length) {
            if (this.orig[this.index] == c) {
                this.index++;
            } else {
                throw new ParseException("Wrong char", this.index);
            }
        } else {
            throw new ParseException("No more characters", this.index);
        }
    }

    public boolean skipIfChar(char c) throws ParseException {
        if (this.index < this.orig.length) {
            if (this.orig[this.index] == c) {
                this.index++;
                return true;
            }
            return false;
        }

        throw new ParseException("No more characters", this.index);
    }

    public int parseNumber() throws ParseException {
        int length = this.orig.length;
        boolean gotNum = false;
        int result = 0;

        while (this.index < length) {
            switch (this.orig[this.index]) {
                case '0':
                    result *= 10;
                    gotNum = true;
                    break;

                case '1':
                    result = result * 10 + 1;
                    gotNum = true;
                    break;

                case '2':
                    result = result * 10 + 2;
                    gotNum = true;
                    break;

                case '3':
                    result = result * 10 + 3;
                    gotNum = true;
                    break;

                case '4':
                    result = result * 10 + 4;
                    gotNum = true;
                    break;

                case '5':
                    result = result * 10 + 5;
                    gotNum = true;
                    break;

                case '6':
                    result = result * 10 + 6;
                    gotNum = true;
                    break;

                case '7':
                    result = result * 10 + 7;
                    gotNum = true;
                    break;

                case '8':
                    result = result * 10 + 8;
                    gotNum = true;
                    break;

                case '9':
                    result = result * 10 + 9;
                    gotNum = true;
                    break;

                default:
                    if (gotNum) {
                        return result;
                    }
                    throw new ParseException("No Number found", this.index);
            }

            this.index++;
        }

        if (gotNum) {
            return result;
        }

        throw new ParseException("No Number found", this.index);
    }

    public int parseMonth() throws ParseException {
        try {
            char curr;
            switch (this.orig[this.index++]) {

                case 'J':
                case 'j':
                    switch (this.orig[this.index++]) {
                        case 'A':
                        case 'a':
                            curr = this.orig[this.index++];
                            if (curr == 'N' || curr == 'n') {
                                return 0;
                            }
                            break;

                        case 'U':
                        case 'u':
                            curr = this.orig[this.index++];
                            if (curr == 'N' || curr == 'n') {
                                return 5;
                            }
                            if (curr == 'L' || curr == 'l') {
                                return 6;
                            }
                            break;
                    }

                    break;
                case 'F':
                case 'f':
                    curr = this.orig[this.index++];
                    if (curr == 'E' || curr == 'e') {
                        curr = this.orig[this.index++];
                        if (curr == 'B' || curr == 'b') {
                            return 1;
                        }
                    }
                    break;

                case 'M':
                case 'm':
                    curr = this.orig[this.index++];
                    if (curr == 'A' || curr == 'a') {
                        curr = this.orig[this.index++];
                        if (curr == 'R' || curr == 'r') {
                            return 2;
                        }
                        if (curr == 'Y' || curr == 'y') {
                            return 4;
                        }
                    }
                    break;

                case 'A':
                case 'a':
                    curr = this.orig[this.index++];
                    if (curr == 'P' || curr == 'p') {
                        curr = this.orig[this.index++];
                        if (curr == 'R' || curr == 'r') {
                            return 3;
                        }
                        break;
                    }
                    if (curr == 'U' || curr == 'u') {
                        curr = this.orig[this.index++];
                        if (curr == 'G' || curr == 'g') {
                            return 7;
                        }
                    }
                    break;

                case 'S':
                case 's':
                    curr = this.orig[this.index++];
                    if (curr == 'E' || curr == 'e') {
                        curr = this.orig[this.index++];
                        if (curr == 'P' || curr == 'p') {
                            return 8;
                        }
                    }
                    break;

                case 'O':
                case 'o':
                    curr = this.orig[this.index++];
                    if (curr == 'C' || curr == 'c') {
                        curr = this.orig[this.index++];
                        if (curr == 'T' || curr == 't') {
                            return 9;
                        }
                    }
                    break;

                case 'N':
                case 'n':
                    curr = this.orig[this.index++];
                    if (curr == 'O' || curr == 'o') {
                        curr = this.orig[this.index++];
                        if (curr == 'V' || curr == 'v') {
                            return 10;
                        }
                    }
                    break;

                case 'D':
                case 'd':
                    curr = this.orig[this.index++];
                    if (curr == 'E' || curr == 'e') {
                        curr = this.orig[this.index++];
                        if (curr == 'C' || curr == 'c') {
                            return 11;
                        }
                    }
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        throw new ParseException("Bad Month", this.index);
    }

    public int parseTimeZone() throws ParseException {
        if (this.index >= this.orig.length) {
            throw new ParseException("No more characters", this.index);
        }
        char test = this.orig[this.index];
        if (test == '+' || test == '-') {
            return parseNumericTimeZone();
        }
        return parseAlphaTimeZone();
    }

    public int parseNumericTimeZone() throws ParseException {
        boolean switchSign = false;
        char first = this.orig[this.index++];
        if (first == '+') {
            switchSign = true;
        } else if (first != '-') {
            throw new ParseException("Bad Numeric TimeZone", this.index);
        }

        int oindex = this.index;
        int tz = parseNumber();
        if (tz >= 2400) {
            throw new ParseException("Numeric TimeZone out of range", oindex);
        }
        int offset = tz / 100 * 60 + tz % 100;
        if (switchSign) {
            return -offset;
        }
        return offset;
    }

    public int parseAlphaTimeZone() throws ParseException {
        int result = 0;
        boolean foundCommon = false;

        try {
            char curr;
            switch (this.orig[this.index++]) {
                case 'U':
                case 'u':
                    curr = this.orig[this.index++];
                    if (curr == 'T' || curr == 't') {
                        result = 0;
                        break;
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);

                case 'G':
                case 'g':
                    curr = this.orig[this.index++];
                    if (curr == 'M' || curr == 'm') {
                        curr = this.orig[this.index++];
                        if (curr == 'T' || curr == 't') {
                            result = 0;
                            break;
                        }
                    }
                    throw new ParseException("Bad Alpha TimeZone", this.index);

                case 'E':
                case 'e':
                    result = 300;
                    foundCommon = true;
                    break;

                case 'C':
                case 'c':
                    result = 360;
                    foundCommon = true;
                    break;

                case 'M':
                case 'm':
                    result = 420;
                    foundCommon = true;
                    break;

                case 'P':
                case 'p':
                    result = 480;
                    foundCommon = true;
                    break;

                default:
                    throw new ParseException("Bad Alpha TimeZone", this.index);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ParseException("Bad Alpha TimeZone", this.index);
        }

        if (foundCommon) {
            char c = this.orig[this.index++];
            if (c == 'S' || c == 's') {
                c = this.orig[this.index++];
                if (c != 'T' && c != 't') {
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
            } else if (c == 'D' || c == 'd') {
                c = this.orig[this.index++];
                if (c == 'T' || c != 't') {

                    result -= 60;
                } else {
                    throw new ParseException("Bad Alpha TimeZone", this.index);
                }
            }
        }

        return result;
    }

    int getIndex() {
        return this.index;
    }

}
