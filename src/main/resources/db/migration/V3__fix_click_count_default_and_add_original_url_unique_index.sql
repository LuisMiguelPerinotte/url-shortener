-- V2 created click_count as BIGSERIAL, which attaches a sequence as its default.
-- A counter must start at 0, so replace the sequence default with a plain one.
ALTER TABLE short_url ALTER COLUMN click_count DROP DEFAULT;

DROP SEQUENCE IF EXISTS short_url_click_count_seq;

ALTER TABLE short_url ALTER COLUMN click_count SET DEFAULT 0;

-- original_url is TEXT, and a btree entry is limited to ~2.7KB, so index its hash instead.
-- Lookups must use md5(original_url) to hit this index (see ShortURLRepository.findByOriginalUrl).
CREATE UNIQUE INDEX ux_short_url_original_url_md5 ON short_url (md5(original_url));
