/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.mail.search;

/**
 *
 * @author abarrime
 */
public abstract class IntegerComparisonTerm extends ComparisonTerm {

    protected int number;
    private static final long serialVersionUID = -6963571240154302484L;

    protected IntegerComparisonTerm() {
        this.comparison = 0;
        this.number = 0;
    }

    protected IntegerComparisonTerm(int comparison, int number) {
        this.comparison = comparison;
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public int getComparison() {
        return this.comparison;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setComparison(int comparison) {
        this.comparison = comparison;
    }

    protected boolean match(int i) {
        switch (this.comparison) {
            case 1:
                return (i <= this.number);
            case 2:
                return (i < this.number);
            case 3:
                return (i == this.number);
            case 4:
                return (i != this.number);
            case 5:
                return (i > this.number);
            case 6:
                return (i >= this.number);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IntegerComparisonTerm)) {
            return false;
        }
        IntegerComparisonTerm ict = (IntegerComparisonTerm) obj;
        return (ict.number == this.number && super.equals(obj));
    }

    @Override
    public int hashCode() {
        return this.number + super.hashCode();
    }

}
