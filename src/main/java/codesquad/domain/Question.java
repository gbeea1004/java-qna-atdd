package codesquad.domain;

import codesquad.CannotDeleteException;
import codesquad.UnAuthorizedException;
import org.hibernate.annotations.Where;
import support.domain.AbstractEntity;
import support.domain.UrlGeneratable;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class Question extends AbstractEntity implements UrlGeneratable {
    @Size(min = 3, max = 100)
    @Column(length = 100, nullable = false)
    private String title;

    @Size(min = 3)
    @Lob
    private String contents;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

//    @Embedded
//    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @Where(clause = "deleted = false")
    @OrderBy("id ASC")
    private List<Answer> answers = new ArrayList<>();

    private boolean deleted = false;

    public Question() {
    }

    public Question(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }

    public Question(String title, String contents, User writer) {
        this(title, contents);
        this.writer = writer;
    }

    public String getTitle() {
        return title;
    }

    public Question setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContents() {
        return contents;
    }

    public Question setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public User getWriter() {
        return writer;
    }

    public void writeBy(User loginUser) {
        this.writer = loginUser;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        answers.add(answer);
    }

    public boolean isOwner(User loginUser) {
        return writer.equals(loginUser);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Question update(User loginUser, Question updatedQuestion) {
        if (!isOwner(loginUser)) {
            throw new UnAuthorizedException();
        }

        this.title = updatedQuestion.title;
        this.contents = updatedQuestion.contents;
        return this;
    }

    public Question delete(User loginUser) {
        if (!isOwner(loginUser)) {
            throw new CannotDeleteException("삭제할 수 없습니다.");
        }

        long otherAnswers = answers.stream().filter(answer -> !answer.isOwner(writer)).filter(answer -> !answer.isDeleted()).count();

        if (otherAnswers > 0) {
            throw new UnAuthorizedException("다른 댓글 작성자가 있습니다.");
        }

        for (Answer answer : answers) {
            answer.delete(loginUser);
        }

        this.deleted = true;
        return this;
    }

    public List<DeleteHistory> deleteHistories(long questionId) {
        List<DeleteHistory> histories = new ArrayList<>();
        histories.add(new DeleteHistory(ContentType.QUESTION, questionId, writer, LocalDateTime.now()));
        for (Answer answer : answers) {
            histories.add(new DeleteHistory(ContentType.ANSWER, answer.getId(), writer, LocalDateTime.now()));
        }
        return histories;
    }

    @Override
    public String generateUrl() {
        return String.format("/questions/%d", getId());
    }

    @Override
    public String toString() {
        return "Question [id=" + getId() + ", title=" + title + ", contents=" + contents + ", writer=" + writer + "]";
    }
}
