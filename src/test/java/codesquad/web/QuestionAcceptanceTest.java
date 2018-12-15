package codesquad.web;

import codesquad.domain.QuestionRepository;
import codesquad.domain.User;
import codesquad.security.HttpSessionUtils;
import codesquad.security.LoginUser;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import support.test.AcceptanceTest;
import support.test.HtmlFormDataBuilder;

import static org.slf4j.LoggerFactory.getLogger;

public class QuestionAcceptanceTest extends AcceptanceTest {
    private static final Logger logger = getLogger(QuestionAcceptanceTest.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    public void 로그인한유저_게시글작성() {
        User loginUser = defaultUser();
        ResponseEntity<String> response = basicAuthTemplate(loginUser)
                .getForEntity("/questions/form", String.class);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        logger.debug("body : {}", response.getBody());
    }

    @Test
    public void 로그인안한유저_게시글작성() {
        ResponseEntity<String> response = template()
                .getForEntity("/questions/form", String.class);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        logger.debug("body : {}", response.getBody());
    }

    @Test
    public void 게시글작성() {
        User loginUser = defaultUser();
        HttpEntity<MultiValueMap<String, Object>> request = HtmlFormDataBuilder.urlEncodingForm()
                .addParameter("title", "제목1")
                .addParameter("contents", "컨텐츠1")
                .build();

        ResponseEntity<String> response = basicAuthTemplate(loginUser)
                .postForEntity("/questions", request, String.class);

        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FOUND);
        softly.assertThat(questionRepository.findByTitle("제목1").isPresent()).isTrue();
        softly.assertThat(response.getHeaders().getLocation().getPath()).startsWith("/");
    }

    @Test
    public void 질문수정_로그인안했을때() {
        ResponseEntity<String> response = template().getForEntity(String.format("/questions/%d/form", defaultUser().getId()), String.class);
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void 질문수정_로그인했을때() {

    }

    @Test
    public void 질문수정_질문자와_로그인유저가_같을때() {

    }


    @Test
    public void 질문삭제_질문자와_로그인유저가_같을때() {

    }

    @Test
    public void 질문삭제_질문자와_로그인유저가_다를때() {

    }

}
