package uncc2014watsonsim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Question extends ArrayList<ResultSet>{
	private static final long serialVersionUID = 1L;
	int id; // Question ID comes from the database and is optional.
	String text, answer, raw_text;
    private String category = "unknown";
    private QType type;
	
    /** Create a question from it's raw text */
	public Question(String text) {
		this.raw_text = text;
		this.text = text.replaceAll("[^0-9a-zA-Z ]+", "").trim();
		this.type = QClassDetection.detectType(this);
	}
	
	/** Create a question given it's raw text and category */
	public Question(String question, String category) {
		this(question);
		this.category = category;
	}
	
	/** Create a question to which the raw text and answer are known but not the category */
	public static Question known(String question, String answer) {
		Question q = new Question(question);
		q.answer = answer;
		return q;
	}
	
	/** Create a question to which the raw text, answer, and category are known*/
	public static Question known(String question, String answer, String category) {
		Question q = new Question(question);
		q.answer = answer;
		q.setCategory(category);
		return q;
	}
	
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public QType getType() {
        return type;
    }

    public void setType(QType type) {
        this.type = type;
    }

	
	@Override
	/** Add a new result candidate */
	public boolean add(ResultSet cand) {
		for (ResultSet existing : this) {
			if (existing.equals(cand)) {
				existing.merge(cand);
				return false;
			}
		}
		super.add(cand);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends ResultSet> c) {
		boolean changed=false;
		for (ResultSet rs : c) changed |= add(rs);
		return changed;
	}

}
