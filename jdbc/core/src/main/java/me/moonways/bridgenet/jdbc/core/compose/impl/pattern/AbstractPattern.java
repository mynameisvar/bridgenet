package me.moonways.bridgenet.jdbc.core.compose.impl.pattern;

import lombok.*;
import me.moonways.bridgenet.jdbc.core.TransactionIsolation;
import me.moonways.bridgenet.jdbc.core.compose.impl.pattern.verification.VerificationContext;
import me.moonways.bridgenet.jdbc.core.compose.impl.pattern.verification.VerificationResult;
import me.moonways.bridgenet.jdbc.core.compose.template.completed.CompletedQuery;
import me.moonways.bridgenet.jdbc.core.DatabaseConnection;
import me.moonways.bridgenet.jdbc.core.ResponseStream;
import me.moonways.bridgenet.jdbc.core.compose.transform.PatternToQueryTransformer;
import me.moonways.bridgenet.jdbc.core.compose.TemplatedQuery;
import me.moonways.bridgenet.jdbc.core.util.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

@ToString
@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class AbstractPattern
        implements TemplatedQuery, CompletedQuery {

    private static final String PATTERN_FILES_RESOURCES_FOLDER = "/patterns/";
    private static final String PATTERN_FILE_SUFFIX = ".pattern";
    
    private final PatternCollections totals;

    private String cachedNativeSql;

    public abstract VerificationResult verify(@NotNull VerificationContext context);

    protected void onPreprocess() {
        // override me.
    }

    @Override
    public CompletedQuery combine() {
        onPreprocess();

        VerificationResult verificationResult = verify(
                new VerificationContext());

        if (!verificationResult.isSuccess())
            cachedNativeSql = verificationResult.toString();

        return this;
    }

    @Override
    public Result<ResponseStream> call(DatabaseConnection connection) {
        return connection.call(toNativeQuery().get());
    }

    @Override
    public Result<ResponseStream> callTransactional(DatabaseConnection connection) {
        return connection.ofTransactionalGet(() -> connection.call(toNativeQuery().get()));
    }

    @Override
    public Result<ResponseStream> callTransactional(TransactionIsolation isolation, DatabaseConnection connection) {
        return connection.ofTransactionalGet(isolation, () -> connection.call(toNativeQuery().get()));
    }

    @SneakyThrows
    @Override
    public final Result<String> toNativeQuery() {
        if (cachedNativeSql != null) {
            return Result.ofExclusive(cachedNativeSql);
        }

        String pattern = totals.getPattern();
        String value = pattern;

        if (pattern.endsWith(PATTERN_FILE_SUFFIX)) {
            value = readPatternFile(pattern);
        }

        PatternToQueryTransformer converter = new PatternToQueryTransformer(value, totals);
        return Result.ofExclusive(cachedNativeSql
                = converter.toNativeQuery());
    }

    @SuppressWarnings({"resource", "DataFlowIssue", "ResultOfMethodCallIgnored"})
    @SneakyThrows
    private String readPatternFile(String filename) {
        String path = PATTERN_FILES_RESOURCES_FOLDER + filename;

        InputStream resourceInputStream
                = AbstractPattern.class.getResourceAsStream(path);

        byte[] array = new byte[resourceInputStream.available()];
        resourceInputStream.read(array);

        return new String(array);
    }
}
